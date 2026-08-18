package com.ruoyi.crm.dingtalk.search.impl;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.dingtalk.search.UserSearchService;
import com.ruoyi.crm.dingtalk.sync.OrgSyncService;
import com.ruoyi.system.api.RemoteDeptService;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysDept;
import com.ruoyi.system.api.domain.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 人员搜索服务实现
 * <p>
 * 基于组织同步后的 sys_user 快照进行搜索，通过 Feign 调用 ruoyi-system。
 * 支持按关键词（姓名/手机号/用户名）和部门搜索，管理员可触发单用户刷新。
 *
 * @author ruoyi-crm
 */
@Service
public class UserSearchServiceImpl implements UserSearchService
{
    private static final Logger log = LoggerFactory.getLogger(UserSearchServiceImpl.class);

    private static final String SOURCE_INNER = SecurityConstants.INNER;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteDeptService remoteDeptService;

    @Autowired
    private OrgSyncService orgSyncService;

    @Override
    public List<SysUser> search(String tenantId, String keyword)
    {
        if (keyword == null || keyword.trim().isEmpty())
        {
            return Collections.emptyList();
        }

        // 用关键词作为 userName 模糊查询条件
        // RuoYi 的 selectUserList 支持 userName 模糊匹配
        SysUser query = new SysUser();
        query.setUserName(keyword);

        R<List<SysUser>> resp = remoteUserService.innerList(query, SOURCE_INNER);
        if (resp != null && R.isSuccess(resp) && resp.getData() != null)
        {
            return resp.getData();
        }
        log.warn("User search by keyword failed: {}", keyword);
        return Collections.emptyList();
    }

    @Override
    public List<SysUser> findByDept(String tenantId, Long deptId)
    {
        if (deptId == null)
        {
            return Collections.emptyList();
        }

        SysUser query = new SysUser();
        query.setDeptId(deptId);

        R<List<SysUser>> resp = remoteUserService.innerList(query, SOURCE_INNER);
        if (resp != null && R.isSuccess(resp) && resp.getData() != null)
        {
            return resp.getData();
        }
        log.warn("User search by dept failed: deptId={}", deptId);
        return Collections.emptyList();
    }

    @Override
    public List<SysUser> findByDeptRecursive(String tenantId, Long deptId)
    {
        if (deptId == null)
        {
            return Collections.emptyList();
        }

        List<SysUser> result = new ArrayList<>();
        // 查询当前部门下的用户
        result.addAll(findByDept(tenantId, deptId));

        // 查询子部门
        R<List<SysDept>> deptResp = remoteDeptService.innerList(SOURCE_INNER);
        if (deptResp != null && R.isSuccess(deptResp) && deptResp.getData() != null)
        {
            for (SysDept dept : deptResp.getData())
            {
                if (deptId.equals(dept.getParentId()))
                {
                    result.addAll(findByDeptRecursive(tenantId, dept.getDeptId()));
                }
            }
        }

        return result;
    }

    @Override
    public SysUser findById(String tenantId, Long userId)
    {
        if (userId == null)
        {
            return null;
        }

        R<SysUser> resp = remoteUserService.innerGetUserById(userId, SOURCE_INNER);
        if (resp != null && R.isSuccess(resp) && resp.getData() != null)
        {
            return resp.getData();
        }
        log.warn("User search by id failed: userId={}", userId);
        return null;
    }

    @Override
    public boolean refreshUser(String tenantId, String dingtalkUserId)
    {
        return orgSyncService.refreshSingleUser(tenantId, dingtalkUserId);
    }
}
