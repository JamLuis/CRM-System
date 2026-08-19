package com.ruoyi.crm.dingtalk.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.dingtalk.service.DingTalkIdentityExchangeService;
import com.ruoyi.crm.dingtalk.sync.OrgSyncService;
import com.ruoyi.crm.dingtalk.sync.OrgSyncService.SyncCursorInfo;
import com.ruoyi.crm.dingtalk.sync.OrgSyncService.SyncResult;
import com.ruoyi.crm.tenant.domain.CrmDingtalkIdentity;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkIdentityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 钉钉管理接口（管理员）
 * <p>
 * 提供组织同步状态查询/手动触发，以及钉钉身份映射授权管理。
 * 管理员可在后台完成「钉钉用户 → 系统用户」映射授权，无需数据库手工操作。
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/dingtalk")
public class DingTalkAdminController
{
    private static final Logger log = LoggerFactory.getLogger(DingTalkAdminController.class);

    @Autowired
    private OrgSyncService orgSyncService;

    @Autowired
    private DingTalkIdentityExchangeService identityExchangeService;

    @Autowired
    private CrmDingtalkIdentityMapper identityMapper;

    // ==================== 组织同步 ====================

    /**
     * 查询组织同步状态（游标、最近同步时间）
     */
    @RequiresPermissions("crm:admin:orgsync")
    @GetMapping("/sync/status")
    public R<SyncCursorInfo> syncStatus()
    {
        String tenantId = TenantContext.getTenantId();
        return R.ok(orgSyncService.getSyncStatus(tenantId));
    }

    /**
     * 手动触发全量同步（部门 + 人员对账）
     */
    @RequiresPermissions("crm:admin:orgsync")
    @PostMapping("/sync/full")
    public R<SyncResult> fullSync()
    {
        String tenantId = TenantContext.getTenantId();
        log.info("Admin triggered full org sync, tenantId={}", tenantId);
        SyncResult result = orgSyncService.fullSync(tenantId);
        return result.success ? R.ok(result) : R.fail("全量同步失败：" + result.error);
    }

    /**
     * 手动触发增量同步（基于游标）
     */
    @RequiresPermissions("crm:admin:orgsync")
    @PostMapping("/sync/incremental")
    public R<SyncResult> incrementalSync()
    {
        String tenantId = TenantContext.getTenantId();
        log.info("Admin triggered incremental org sync, tenantId={}", tenantId);
        SyncResult result = orgSyncService.incrementalSync(tenantId);
        return result.success ? R.ok(result) : R.fail("增量同步失败：" + result.error);
    }

    // ==================== 身份映射授权 ====================

    /**
     * 查询全部身份映射
     */
    @RequiresPermissions("crm:admin:grant")
    @GetMapping("/identities")
    public R<List<CrmDingtalkIdentity>> listIdentities()
    {
        String tenantId = TenantContext.getTenantId();
        return R.ok(identityMapper.selectAll(tenantId));
    }

    /**
     * 按钉钉用户 ID 查询身份映射
     */
    @RequiresPermissions("crm:admin:grant")
    @GetMapping("/identities/by-dingtalk-user")
    public R<CrmDingtalkIdentity> getIdentityByDingtalkUser(@RequestParam("dingtalkUserId") String dingtalkUserId)
    {
        String tenantId = TenantContext.getTenantId();
        CrmDingtalkIdentity identity = identityMapper.selectByDingtalkUserId(tenantId, dingtalkUserId);
        return identity != null ? R.ok(identity) : R.fail("该钉钉用户尚未映射");
    }

    /**
     * 创建或更新身份映射（授权钉钉用户访问 CRM）
     * <p>
     * 请求体：{ "dingtalkUserId": "...", "sysUserId": 123, "unionId": "可选" }
     */
    @RequiresPermissions("crm:admin:grant")
    @PostMapping("/identities/map")
    public R<Void> mapIdentity(@RequestBody Map<String, Object> body)
    {
        String dingtalkUserId = (String) body.get("dingtalkUserId");
        Object sysUserIdObj = body.get("sysUserId");
        String unionId = (String) body.get("unionId");

        if (dingtalkUserId == null || dingtalkUserId.trim().isEmpty())
        {
            return R.fail("dingtalkUserId 不能为空");
        }
        if (sysUserIdObj == null)
        {
            return R.fail("sysUserId 不能为空");
        }
        Long sysUserId = Long.valueOf(String.valueOf(sysUserIdObj));

        String tenantId = TenantContext.getTenantId();
        identityExchangeService.mapIdentity(tenantId, dingtalkUserId.trim(), sysUserId, unionId);
        log.info("Admin mapped DingTalk identity: dingtalkUserId={}, sysUserId={}", dingtalkUserId, sysUserId);
        return R.ok();
    }
}
