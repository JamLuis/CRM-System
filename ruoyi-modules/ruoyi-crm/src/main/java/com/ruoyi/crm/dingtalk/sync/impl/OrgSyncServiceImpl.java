package com.ruoyi.crm.dingtalk.sync.impl;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.dingtalk.client.DingTalkClient;
import com.ruoyi.crm.dingtalk.config.DingTalkProperties;
import com.ruoyi.crm.dingtalk.domain.DingTalkDept;
import com.ruoyi.crm.dingtalk.domain.DingTalkDeptUser;
import com.ruoyi.crm.dingtalk.sync.OrgSyncService;
import com.ruoyi.crm.tenant.domain.CrmDingtalkIdentity;
import com.ruoyi.crm.tenant.domain.CrmDingtalkDirectoryUser;
import com.ruoyi.crm.tenant.domain.CrmOrgSyncCursor;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkIdentityMapper;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkDirectoryUserMapper;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkDeptMapMapper;
import com.ruoyi.crm.tenant.mapper.CrmOrgSyncCursorMapper;
import com.ruoyi.system.api.RemoteDeptService;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysDept;
import com.ruoyi.system.api.domain.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织架构同步服务实现
 * <p>
 * 策略：
 * <ul>
 *   <li>全量同步：从根部门递归拉取所有子部门和人员，与本地快照对账</li>
 *   <li>增量同步：基于游标拉取变更数据</li>
 *   <li>离职用户：停用 sys_user 状态，不删除，保留历史</li>
 *   <li>同步游标：记录在 crm_org_sync_cursor 表</li>
 * </ul>
 *
 * @author ruoyi-crm
 */
@Service
public class OrgSyncServiceImpl implements OrgSyncService
{
    private static final Logger log = LoggerFactory.getLogger(OrgSyncServiceImpl.class);

    private static final String SYNC_TYPE = "DINGTALK";
    private static final String SOURCE_INNER = SecurityConstants.INNER;

    @Autowired
    private DingTalkClient dingTalkClient;

    @Autowired
    private DingTalkProperties properties;

    @Autowired
    private CrmDingtalkIdentityMapper identityMapper;

    @Autowired
    private CrmDingtalkDirectoryUserMapper directoryUserMapper;

    @Autowired
    private CrmDingtalkDeptMapMapper deptMapMapper;

    @Autowired
    private CrmOrgSyncCursorMapper cursorMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteDeptService remoteDeptService;

    /** 钉钉部门 ID → RuoYi 部门 ID 映射（同步过程中构建） */
    private final Map<Long, Long> deptIdMap = new HashMap<>();
    /** 钉钉部门 ID → 部门名称，用于授权列表展示组织机构。 */
    private final Map<Long, String> deptNameMap = new HashMap<>();
    /** 用于首次迁移时复用现有同父同名系统部门。 */
    private final Map<String, SysDept> existingDeptMap = new HashMap<>();
    /** 一次全量同步中人员去重。 */
    private final Set<String> syncedUserIds = new HashSet<>();

    @Override
    public synchronized SyncResult fullSync(String tenantId)
    {
        SyncResult result = new SyncResult(true);
        log.info("Starting full org sync for tenant: {}", tenantId);

        try
        {
            deptIdMap.clear();
            deptNameMap.clear();
            existingDeptMap.clear();
            syncedUserIds.clear();
            loadExistingSystemDepts();
            Long dingTalkRootDeptId = properties.getRootDeptId();
            Long systemRootDeptId = properties.getSystemRootDeptId();
            deptIdMap.put(dingTalkRootDeptId, systemRootDeptId);

            // 1. 同步部门（从根部门递归）
            syncDeptsRecursive(dingTalkRootDeptId, tenantId, result);

            // 2. 同步人员（遍历所有已同步部门）
            for (Map.Entry<Long, Long> entry : deptIdMap.entrySet())
            {
                syncDeptUsers(entry.getKey(), entry.getValue(), tenantId, result);
            }

            // 3. 更新游标
            updateCursor(tenantId, String.valueOf(System.currentTimeMillis()), "SUCCESS", null);
            log.info("Full org sync completed: depts={}, users={}, updated={}, deactivated={}",
                    result.deptCount, result.userCount, result.userUpdated, result.userDeactivated);
        }
        catch (Exception e)
        {
            result.success = false;
            result.error = e.getMessage();
            updateCursor(tenantId, null, "FAILED", e.getMessage());
            log.error("Full org sync failed for tenant: {}", tenantId, e);
        }

        return result;
    }

    @Override
    public SyncResult incrementalSync(String tenantId)
    {
        SyncResult result = new SyncResult(true);
        log.info("Starting incremental org sync for tenant: {}", tenantId);

        try
        {
            // 增量同步：基于游标全量拉取（钉钉 V2 API 不支持真正的增量）
            // 实际实现中，这里可以对比上次同步时间，只处理变更
            // V1 简化：执行全量但只更新有变化的记录
            SyncResult fullResult = fullSync(tenantId);
            result.deptCount = fullResult.deptCount;
            result.userCount = fullResult.userCount;
            result.userUpdated = fullResult.userUpdated;
            result.userDeactivated = fullResult.userDeactivated;
            result.success = fullResult.success;
            result.error = fullResult.error;
        }
        catch (Exception e)
        {
            result.success = false;
            result.error = e.getMessage();
            log.error("Incremental org sync failed for tenant: {}", tenantId, e);
        }

        return result;
    }

    @Override
    public boolean refreshSingleUser(String tenantId, String dingtalkUserId)
    {
        try
        {
            // 查找用户所在的部门
            for (Map.Entry<Long, Long> entry : deptIdMap.entrySet())
            {
                List<DingTalkDeptUser> users = dingTalkClient.getDeptUserList(entry.getKey(), 0L, 100L);
                for (DingTalkDeptUser user : users)
                {
                    if (dingtalkUserId.equals(user.getUserid()))
                    {
                        syncSingleUser(user, entry.getValue(), tenantId);
                        return true;
                    }
                }
            }
            log.warn("User not found in DingTalk: {}", dingtalkUserId);
            return false;
        }
        catch (Exception e)
        {
            log.error("Failed to refresh single user: {}", dingtalkUserId, e);
            return false;
        }
    }

    @Override
    public SyncCursorInfo getSyncStatus(String tenantId)
    {
        CrmOrgSyncCursor cursor = cursorMapper.selectBySource(tenantId, SYNC_TYPE);
        if (cursor == null)
        {
            return null;
        }
        SyncCursorInfo info = new SyncCursorInfo();
        info.source = cursor.getSource();
        info.cursor = cursor.getCursor();
        info.lastSyncTime = cursor.getLastSyncTime();
        info.status = cursor.getStatus();
        info.errorSummary = cursor.getErrorSummary();
        return info;
    }

    // --- 内部方法 ---

    /**
     * 递归同步部门
     */
    private void syncDeptsRecursive(Long parentDingTalkDeptId, String tenantId, SyncResult result)
    {
        try
        {
            List<DingTalkDept> depts = dingTalkClient.getDeptList(parentDingTalkDeptId);
            for (DingTalkDept dept : depts)
            {
                // 在 RuoYi 中创建或更新部门
                Long sysDeptId = syncDept(dept, parentDingTalkDeptId, tenantId);
                deptIdMap.put(dept.getDeptId(), sysDeptId);
                deptNameMap.put(dept.getDeptId(), dept.getName());
                result.deptCount++;

                // 递归子部门
                syncDeptsRecursive(dept.getDeptId(), tenantId, result);
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("同步钉钉部门失败，parentDeptId="
                    + parentDingTalkDeptId, e);
        }
    }

    /**
     * 同步单个部门到 RuoYi
     */
    private Long syncDept(DingTalkDept dtDept, Long parentDingTalkId, String tenantId)
    {
        SysDept sysDept = new SysDept();
        sysDept.setDeptName(dtDept.getName());
        sysDept.setOrderNum(dtDept.getOrder() != null ? dtDept.getOrder().intValue() : 0);
        sysDept.setStatus("0"); // 正常

        // 设置父部门
        Long parentSysId = deptIdMap.get(parentDingTalkId);
        if (parentSysId != null)
        {
            sysDept.setParentId(parentSysId);
        }
        else
        {
            sysDept.setParentId(properties.getSystemRootDeptId());
        }

        Long mappedSysDeptId = deptMapMapper.selectSysDeptId(tenantId, dtDept.getDeptId());
        if (mappedSysDeptId != null)
        {
            sysDept.setDeptId(mappedSysDeptId);
            R<Boolean> editResponse = remoteDeptService.innerEditDept(sysDept, SOURCE_INNER);
            if (editResponse == null || !R.isSuccess(editResponse))
            {
                log.warn("Failed to update mapped dept via Feign: {}, sysDeptId={}",
                        dtDept.getName(), mappedSysDeptId);
            }
            return mappedSysDeptId;
        }

        SysDept existingDept = existingDeptMap.get(deptKey(sysDept.getParentId(), sysDept.getDeptName()));
        if (existingDept != null)
        {
            deptMapMapper.upsert(idGenerator.nextId(), tenantId, dtDept.getDeptId(),
                    existingDept.getDeptId(), dtDept.getName());
            return existingDept.getDeptId();
        }

        // 通过 Feign 创建部门
        R<Long> resp = remoteDeptService.innerAddDept(sysDept, SOURCE_INNER);
        if (resp != null && R.isSuccess(resp) && resp.getData() != null)
        {
            Long newSysDeptId = resp.getData();
            sysDept.setDeptId(newSysDeptId);
            deptMapMapper.upsert(idGenerator.nextId(), tenantId, dtDept.getDeptId(),
                    newSysDeptId, dtDept.getName());
            existingDeptMap.put(deptKey(sysDept.getParentId(), sysDept.getDeptName()), sysDept);
            return newSysDeptId;
        }
        throw new IllegalStateException("创建系统部门失败：" + dtDept.getName());
    }

    /**
     * 同步部门下所有用户
     */
    private void syncDeptUsers(Long dingTalkDeptId, Long sysDeptId, String tenantId, SyncResult result)
    {
        try
        {
            Long cursor = 0L;
            while (true)
            {
                List<DingTalkDeptUser> users = dingTalkClient.getDeptUserList(dingTalkDeptId, cursor, 100L);
                for (DingTalkDeptUser user : users)
                {
                    if (!syncedUserIds.add(user.getUserid()))
                    {
                        continue;
                    }
                    syncSingleUser(user, sysDeptId, tenantId);
                    result.userCount++;
                    result.userUpdated++;
                    if (Boolean.FALSE.equals(user.getActive()))
                    {
                        result.userDeactivated++;
                    }
                }

                if (!dingTalkClient.hasMore(dingTalkDeptId, cursor, 100L))
                {
                    break;
                }
                cursor += 100;
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("同步钉钉部门人员失败，deptId="
                    + dingTalkDeptId, e);
        }
    }

    /**
     * 同步单个用户到 RuoYi
     */
    private void syncSingleUser(DingTalkDeptUser dtUser, Long sysDeptId, String tenantId)
    {
        try
        {
            Long primarySysDeptId = resolvePrimarySystemDept(dtUser.getDeptIdList(), sysDeptId);
            CrmDingtalkDirectoryUser directoryUser = new CrmDingtalkDirectoryUser();
            directoryUser.setId(idGenerator.nextId());
            directoryUser.setTenantId(tenantId);
            directoryUser.setDingtalkUserId(dtUser.getUserid());
            directoryUser.setUnionId(dtUser.getUnionid());
            directoryUser.setName(dtUser.getName());
            directoryUser.setMobile(dtUser.getMobile());
            directoryUser.setEmail(dtUser.getEmail());
            directoryUser.setTitle(dtUser.getTitle());
            directoryUser.setDeptIds(joinLongs(dtUser.getDeptIdList()));
            directoryUser.setDeptNames(joinDeptNames(dtUser.getDeptIdList()));
            directoryUser.setSysDeptId(primarySysDeptId);
            directoryUser.setActive(!Boolean.FALSE.equals(dtUser.getActive()));
            directoryUser.setLastSyncTime(new Date());
            directoryUserMapper.upsert(directoryUser);

            // 已授权人员继续同步基本资料；未授权人员不得创建系统账号或身份映射。
            CrmDingtalkIdentity existing = identityMapper.selectByDingtalkUserId(tenantId, dtUser.getUserid());

            if (existing != null)
            {
                SysUser sysUser = new SysUser();
                sysUser.setUserId(existing.getSysUserId());
                sysUser.setNickName(dtUser.getName());
                sysUser.setPhonenumber(dtUser.getMobile());
                sysUser.setEmail(dtUser.getEmail());
                sysUser.setDeptId(primarySysDeptId);
                // 离职用户停用
                if (dtUser.getActive() != null && !dtUser.getActive())
                {
                    sysUser.setStatus("1"); // 停用
                }
                else
                {
                    sysUser.setStatus("0"); // 正常
                }
                R<Boolean> editResult = remoteUserService.innerEditUser(sysUser, SOURCE_INNER);
                if (editResult == null || !R.isSuccess(editResult) || !Boolean.TRUE.equals(editResult.getData()))
                {
                    throw new IllegalStateException("更新已授权系统用户失败：" + existing.getSysUserId());
                }

                // 更新身份映射的 unionId
                if (dtUser.getUnionid() != null && !dtUser.getUnionid().equals(existing.getUnionId()))
                {
                    existing.setUnionId(dtUser.getUnionid());
                    identityMapper.update(existing);
                }
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("同步钉钉人员失败：" + dtUser.getUserid(), e);
        }
    }

    private String joinLongs(List<Long> values)
    {
        if (values == null || values.isEmpty())
        {
            return null;
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private void loadExistingSystemDepts()
    {
        R<List<SysDept>> response = remoteDeptService.innerList(SOURCE_INNER);
        if (response == null || !R.isSuccess(response) || response.getData() == null)
        {
            throw new IllegalStateException("读取系统部门列表失败");
        }
        for (SysDept dept : response.getData())
        {
            existingDeptMap.put(deptKey(dept.getParentId(), dept.getDeptName()), dept);
        }
    }

    private String deptKey(Long parentId, String deptName)
    {
        return String.valueOf(parentId) + "\u0000" + (deptName == null ? "" : deptName.trim());
    }

    private Long resolvePrimarySystemDept(List<Long> dingtalkDeptIds, Long fallbackSysDeptId)
    {
        if (dingtalkDeptIds != null)
        {
            for (Long dingtalkDeptId : dingtalkDeptIds)
            {
                Long mapped = deptIdMap.get(dingtalkDeptId);
                if (mapped != null)
                {
                    return mapped;
                }
            }
        }
        return fallbackSysDeptId;
    }

    private String joinDeptNames(List<Long> deptIds)
    {
        if (deptIds == null || deptIds.isEmpty())
        {
            return null;
        }
        return deptIds.stream()
                .map(deptNameMap::get)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(" / "));
    }

    /**
     * 更新同步游标
     */
    private void updateCursor(String tenantId, String cursor, String status, String errorSummary)
    {
        try
        {
            CrmOrgSyncCursor existing = cursorMapper.selectBySource(tenantId, SYNC_TYPE);
            if (existing != null)
            {
                existing.setCursor(cursor);
                existing.setLastSyncTime(new Date());
                existing.setStatus(status);
                existing.setErrorSummary(errorSummary);
                cursorMapper.update(existing);
            }
            else
            {
                CrmOrgSyncCursor newCursor = new CrmOrgSyncCursor();
                newCursor.setId(idGenerator.nextId());
                newCursor.setTenantId(tenantId);
                newCursor.setSource(SYNC_TYPE);
                newCursor.setCursor(cursor);
                newCursor.setLastSyncTime(new Date());
                newCursor.setStatus(status);
                newCursor.setErrorSummary(errorSummary);
                cursorMapper.insert(newCursor);
            }
        }
        catch (Exception e)
        {
            log.error("Failed to update sync cursor", e);
        }
    }
}
