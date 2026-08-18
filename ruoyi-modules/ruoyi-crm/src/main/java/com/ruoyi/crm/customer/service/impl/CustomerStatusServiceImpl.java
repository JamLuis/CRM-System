package com.ruoyi.crm.customer.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.*;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.customer.service.CustomerStatusService;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionDeniedException;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 客户经营状态机服务实现
 *
 * @author ruoyi-crm
 */
@Service
public class CustomerStatusServiceImpl implements CustomerStatusService
{
    private static final Logger log = LoggerFactory.getLogger(CustomerStatusServiceImpl.class);

    @Autowired
    private CrmCustomerMapper customerMapper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private CustomerTimelineService timelineService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmCustomer pause(Long customerId, String reason, Date plannedResumeAt)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 状态校验：只有正常状态才能暂停
        if (!OperatingStatus.NORMAL.getValue().equals(customer.getOperatingStatus()))
        {
            throw new IllegalStateException("只有正常状态的客户才能暂停跟进，当前状态：" + customer.getOperatingStatus());
        }

        // 权限校验：主负责人可暂停
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_STATUS);
        permissionService.check(ctx);

        // 乐观锁更新
        int rows = customerMapper.updateOperatingStatus(tenantId, customerId,
                OperatingStatus.PAUSED.getValue(), reason, plannedResumeAt, null,
                operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }

        recordAudit(tenantId, customerId, operatorId, operatorName,
                "PAUSE", OperatingStatus.NORMAL.getValue(), OperatingStatus.PAUSED.getValue());
        recordTimeline(tenantId, customerId, operatorId, operatorName,
                "STATUS_PAUSED", reason);

        log.info("Customer paused: tenantId={}, customerId={}, reason={}, operator={}",
                tenantId, customerId, reason, operatorName);

        return customerMapper.selectByCustomerId(tenantId, customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmCustomer resume(Long customerId, String reason)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 状态校验：只有暂停状态才能恢复
        if (!OperatingStatus.PAUSED.getValue().equals(customer.getOperatingStatus()))
        {
            throw new IllegalStateException("只有暂停跟进状态的客户才能恢复，当前状态：" + customer.getOperatingStatus());
        }

        // 权限校验：主负责人可恢复
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_STATUS);
        permissionService.check(ctx);

        int rows = customerMapper.updateOperatingStatus(tenantId, customerId,
                OperatingStatus.NORMAL.getValue(), reason, null, null,
                operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }

        recordAudit(tenantId, customerId, operatorId, operatorName,
                "RESUME", OperatingStatus.PAUSED.getValue(), OperatingStatus.NORMAL.getValue());
        recordTimeline(tenantId, customerId, operatorId, operatorName,
                "STATUS_RESUMED", reason);

        log.info("Customer resumed: tenantId={}, customerId={}, reason={}, operator={}",
                tenantId, customerId, reason, operatorName);

        return customerMapper.selectByCustomerId(tenantId, customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmCustomer invalidate(Long customerId, String reason)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 状态校验：正常或暂停状态才能失效
        String currentStatus = customer.getOperatingStatus();
        if (!OperatingStatus.NORMAL.getValue().equals(currentStatus)
                && !OperatingStatus.PAUSED.getValue().equals(currentStatus))
        {
            throw new IllegalStateException("只有正常或暂停跟进状态的客户才能设为失效，当前状态：" + currentStatus);
        }

        // 权限校验：销售主管或管理员可执行
        // 通过 DEPT 或 ALL 数据范围判断
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_STATUS);
        if (!isAdmin && !ctx.isSameDept())
        {
            throw new PermissionDeniedException("仅销售主管或管理员可执行失效操作");
        }
        permissionService.check(ctx);

        int rows = customerMapper.updateOperatingStatus(tenantId, customerId,
                OperatingStatus.EXPIRED.getValue(), reason, null, null,
                operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }

        recordAudit(tenantId, customerId, operatorId, operatorName,
                "INVALIDATE", currentStatus, OperatingStatus.EXPIRED.getValue());
        recordTimeline(tenantId, customerId, operatorId, operatorName,
                "STATUS_INVALIDATED", reason);

        log.info("Customer invalidated: tenantId={}, customerId={}, reason={}, operator={}",
                tenantId, customerId, reason, operatorName);

        return customerMapper.selectByCustomerId(tenantId, customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmCustomer archive(Long customerId, String reason)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();

        // 仅管理员可归档
        if (!SecurityUtils.isAdmin(operatorId))
        {
            throw new PermissionDeniedException("仅管理员可归档客户");
        }

        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 状态校验：正常、暂停或失效状态才能归档
        String currentStatus = customer.getOperatingStatus();
        if (OperatingStatus.ARCHIVED.getValue().equals(currentStatus))
        {
            throw new IllegalStateException("客户已归档，无需重复归档");
        }

        int rows = customerMapper.updateOperatingStatus(tenantId, customerId,
                OperatingStatus.ARCHIVED.getValue(), reason, null, new Date(),
                operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }

        recordAudit(tenantId, customerId, operatorId, operatorName,
                "ARCHIVE", currentStatus, OperatingStatus.ARCHIVED.getValue());
        recordTimeline(tenantId, customerId, operatorId, operatorName,
                "STATUS_ARCHIVED", reason);

        log.info("Customer archived: tenantId={}, customerId={}, reason={}, operator={}",
                tenantId, customerId, reason, operatorName);

        return customerMapper.selectByCustomerId(tenantId, customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmCustomer restoreFromArchive(Long customerId, String reason)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();

        // 仅管理员可恢复归档
        if (!SecurityUtils.isAdmin(operatorId))
        {
            throw new PermissionDeniedException("仅管理员可恢复归档客户");
        }

        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 状态校验：只有归档状态才能恢复
        if (!OperatingStatus.ARCHIVED.getValue().equals(customer.getOperatingStatus()))
        {
            throw new IllegalStateException("只有已归档的客户才能恢复，当前状态：" + customer.getOperatingStatus());
        }

        int rows = customerMapper.updateOperatingStatus(tenantId, customerId,
                OperatingStatus.NORMAL.getValue(), reason, null, null,
                operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }

        recordAudit(tenantId, customerId, operatorId, operatorName,
                "RESTORE_ARCHIVE", OperatingStatus.ARCHIVED.getValue(), OperatingStatus.NORMAL.getValue());
        recordTimeline(tenantId, customerId, operatorId, operatorName,
                "STATUS_RESTORED_FROM_ARCHIVE", reason);

        log.info("Customer restored from archive: tenantId={}, customerId={}, reason={}, operator={}",
                tenantId, customerId, reason, operatorName);

        return customerMapper.selectByCustomerId(tenantId, customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmCustomer restoreFromInvalid(Long customerId, String reason)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 状态校验：只有失效状态才能恢复
        if (!OperatingStatus.EXPIRED.getValue().equals(customer.getOperatingStatus()))
        {
            throw new IllegalStateException("只有已失效的客户才能恢复，当前状态：" + customer.getOperatingStatus());
        }

        // 权限校验：销售主管或管理员可恢复
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_STATUS);
        if (!isAdmin && !ctx.isSameDept())
        {
            throw new PermissionDeniedException("仅销售主管或管理员可恢复失效客户");
        }
        permissionService.check(ctx);

        // 恢复后状态为正常，生命周期阶段重置为"待跟进"
        int rows = customerMapper.updateOperatingStatus(tenantId, customerId,
                OperatingStatus.NORMAL.getValue(), reason, null, null,
                operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }

        // 更新生命周期阶段为"待跟进"
        CrmCustomer update = new CrmCustomer();
        update.setCustomerId(customerId);
        update.setTenantId(tenantId);
        update.setLifecycleStage(LifecycleStage.PENDING.getValue());
        update.setVersion(customer.getVersion() + 1);
        update.setUpdateBy(operatorName);
        customerMapper.update(update);

        recordAudit(tenantId, customerId, operatorId, operatorName,
                "RESTORE_INVALID", OperatingStatus.EXPIRED.getValue(), OperatingStatus.NORMAL.getValue());
        recordTimeline(tenantId, customerId, operatorId, operatorName,
                "STATUS_RESTORED_FROM_INVALID", reason);

        log.info("Customer restored from invalid: tenantId={}, customerId={}, reason={}, operator={}",
                tenantId, customerId, reason, operatorName);

        return customerMapper.selectByCustomerId(tenantId, customerId);
    }

    // ==================== Private helpers ====================

    private PermissionContext buildPermissionContext(CrmCustomer customer, Long operatorId,
                                                    Long operatorDeptId, boolean isAdmin,
                                                    PermissionCode permissionCode)
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(operatorId);
        ctx.setOperatorDeptId(operatorDeptId);
        ctx.setAdmin(isAdmin);
        ctx.setPermissionCode(permissionCode);
        ctx.setPrimaryOwnerId(customer.getPrimaryOwnerId());
        ctx.setCollaboratorIds(customer.getCollaboratorIds());
        ctx.setCreatorDeptId(customer.getCreatorDeptId());
        ctx.setOwnerDeptId(customer.getOwnerDeptId());
        ctx.setOperatingStatus(customer.getOperatingStatus());
        return ctx;
    }

    private void recordAudit(String tenantId, Long customerId, Long operatorId,
                             String operatorName, String action, String beforeData, String afterData)
    {
        CrmAuditEvent event = new CrmAuditEvent();
        event.setTenantId(tenantId);
        event.setEventType("CUSTOMER_STATUS");
        event.setEntityType("CUSTOMER");
        event.setEntityId(String.valueOf(customerId));
        event.setOperatorId(operatorId);
        event.setOperatorName(operatorName);
        event.setAction(action);
        event.setBeforeData(beforeData);
        event.setAfterData(afterData);
        auditEventService.record(event);
    }

    private void recordTimeline(String tenantId, Long customerId, Long operatorId,
                                String operatorName, String eventType, String eventData)
    {
        CrmCustomerTimeline timeline = new CrmCustomerTimeline();
        timeline.setTenantId(tenantId);
        timeline.setCustomerId(customerId);
        timeline.setEventType(eventType);
        timeline.setEventData(eventData);
        timeline.setOperatorId(operatorId);
        timeline.setOperatorName(operatorName);
        timelineService.record(timeline);
    }
}
