package com.ruoyi.crm.dingtalk.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.dingtalk.domain.CrmAccessGrantRequest;
import com.ruoyi.crm.dingtalk.service.CrmAccessGrantService;
import com.ruoyi.crm.dingtalk.sync.OrgSyncService;
import com.ruoyi.crm.dingtalk.sync.OrgSyncService.SyncCursorInfo;
import com.ruoyi.crm.dingtalk.sync.OrgSyncService.SyncResult;
import com.ruoyi.crm.tenant.domain.CrmDingtalkDirectoryUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.ruoyi.system.api.domain.SysRole;

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
    private CrmAccessGrantService accessGrantService;

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

    /** 查询企业通讯录人员及其 CRM 角色、权限和授权状态。 */
    @RequiresPermissions("crm:admin:grant")
    @GetMapping("/directory-users")
    public R<List<CrmDingtalkDirectoryUser>> listDirectoryUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "accessStatus", required = false) String accessStatus)
    {
        return R.ok(accessGrantService.listDirectoryUsers(
                TenantContext.getTenantId(), keyword, accessStatus));
    }

    @RequiresPermissions("crm:admin:grant")
    @GetMapping("/access-roles")
    public R<List<SysRole>> listAssignableRoles()
    {
        return R.ok(accessGrantService.listAssignableRoles());
    }

    /** 为已同步的企业人员分配 CRM 角色；身份映射由服务端自动维护。 */
    @RequiresPermissions("crm:admin:grant")
    @PutMapping("/directory-users/{dingtalkUserId}/access")
    public R<Long> grantAccess(@PathVariable String dingtalkUserId,
                               @RequestBody CrmAccessGrantRequest request)
    {
        Long sysUserId = accessGrantService.grant(
                TenantContext.getTenantId(), dingtalkUserId, request.getRoleIds());
        log.info("Admin granted CRM access: dingtalkUserId={}, sysUserId={}",
                dingtalkUserId, sysUserId);
        return R.ok(sysUserId);
    }

    /** 撤销 CRM 角色和免登身份映射，但保留通讯录人员快照。 */
    @RequiresPermissions("crm:admin:grant")
    @DeleteMapping("/directory-users/{dingtalkUserId}/access")
    public R<Void> revokeAccess(@PathVariable String dingtalkUserId)
    {
        accessGrantService.revoke(TenantContext.getTenantId(), dingtalkUserId);
        log.info("Admin revoked CRM access: dingtalkUserId={}", dingtalkUserId);
        return R.ok();
    }
}
