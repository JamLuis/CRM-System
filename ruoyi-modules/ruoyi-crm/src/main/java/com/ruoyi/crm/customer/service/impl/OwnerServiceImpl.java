package com.ruoyi.crm.customer.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.*;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.customer.mapper.CrmCustomerOwnerMapper;
import com.ruoyi.crm.customer.mapper.CrmOwnerChangeMapper;
import com.ruoyi.crm.customer.service.OwnerService;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 客户成员维护与移交服务实现
 *
 * @author ruoyi-crm
 */
@Service
public class OwnerServiceImpl implements OwnerService
{
    private static final Logger log = LoggerFactory.getLogger(OwnerServiceImpl.class);

    @Autowired
    private CrmCustomerMapper customerMapper;

    @Autowired
    private CrmCustomerOwnerMapper ownerMapper;

    @Autowired
    private CrmOwnerChangeMapper changeMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private CustomerTimelineService timelineService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmOwnerChange transfer(Long customerId, Long targetOwnerId, String targetOwnerName,
                                   Long targetOwnerDeptId, boolean keepPreviousAsCollaborator, String reason)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 1. 查询客户
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 2. 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_ASSIGN);
        permissionService.check(ctx);

        // 3. 记录原负责人信息
        Long previousOwnerId = customer.getPrimaryOwnerId();
        String previousOwnerName = customer.getPrimaryOwnerName();

        // 4. 计算新的协同人列表
        String newCollaboratorIds = customer.getCollaboratorIds();
        if (keepPreviousAsCollaborator && previousOwnerId != null)
        {
            newCollaboratorIds = addCollaboratorId(customer.getCollaboratorIds(), previousOwnerId);
        }

        // 5. 乐观锁更新客户主负责人
        int rows = customerMapper.updatePrimaryOwner(tenantId, customerId,
                targetOwnerId, targetOwnerName, targetOwnerDeptId, newCollaboratorIds,
                operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，移交失败，请刷新后重试");
        }

        // 6. 更新成员关系表
        // 6a. 将原 PRIMARY 置为 INACTIVE
        if (previousOwnerId != null)
        {
            ownerMapper.deactivateByCustomerAndRole(tenantId, customerId,
                    OwnerRoleType.PRIMARY.name(), operatorName);
        }

        // 6b. 如果保留为协同人，插入新的 COLLABORATOR 记录
        if (keepPreviousAsCollaborator && previousOwnerId != null)
        {
            CrmCustomerOwner collab = new CrmCustomerOwner();
            collab.setId(idGenerator.nextId());
            collab.setTenantId(tenantId);
            collab.setCustomerId(customerId);
            collab.setUserId(previousOwnerId);
            collab.setUserName(previousOwnerName);
            collab.setRoleType(OwnerRoleType.COLLABORATOR.name());
            collab.setStatus("ACTIVE");
            collab.setCreateBy(operatorName);
            collab.setUpdateBy(operatorName);
            collab.setVersion(0);
            collab.setDelFlag("0");
            ownerMapper.insert(collab);
        }

        // 6c. 插入新 PRIMARY 记录
        CrmCustomerOwner newPrimary = new CrmCustomerOwner();
        newPrimary.setId(idGenerator.nextId());
        newPrimary.setTenantId(tenantId);
        newPrimary.setCustomerId(customerId);
        newPrimary.setUserId(targetOwnerId);
        newPrimary.setUserName(targetOwnerName);
        newPrimary.setRoleType(OwnerRoleType.PRIMARY.name());
        newPrimary.setStatus("ACTIVE");
        newPrimary.setCreateBy(operatorName);
        newPrimary.setUpdateBy(operatorName);
        newPrimary.setVersion(0);
        newPrimary.setDelFlag("0");
        ownerMapper.insert(newPrimary);

        // 7. 记录变更记录
        CrmOwnerChange change = new CrmOwnerChange();
        change.setId(idGenerator.nextId());
        change.setTenantId(tenantId);
        change.setCustomerId(customerId);
        change.setChangeType(OwnerChangeType.TRANSFER.name());
        change.setPreviousPrimaryOwnerId(previousOwnerId);
        change.setPreviousPrimaryOwnerName(previousOwnerName);
        change.setTargetPrimaryOwnerId(targetOwnerId);
        change.setTargetPrimaryOwnerName(targetOwnerName);
        change.setKeepPreviousAsCollaborator(keepPreviousAsCollaborator);
        change.setReason(reason);
        change.setOperatorId(operatorId);
        change.setOperatorName(operatorName);
        changeMapper.insert(change);

        // 8. 审计 + 时间线
        recordAudit(tenantId, customerId, operatorId, operatorName, "TRANSFER",
                previousOwnerName, targetOwnerName);
        recordTimeline(tenantId, customerId, operatorId, operatorName, "OWNER_TRANSFER",
                previousOwnerName + " → " + targetOwnerName);

        log.info("Customer transferred: tenantId={}, customerId={}, from={} to={}, operator={}",
                tenantId, customerId, previousOwnerName, targetOwnerName, operatorName);

        return change;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmOwnerChange assign(Long customerId, Long ownerId, String ownerName,
                                 Long ownerDeptId, Long operatorId, String operatorName)
    {
        String tenantId = TenantContext.getTenantId();

        // 查询客户
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 乐观锁更新客户主负责人
        int rows = customerMapper.updatePrimaryOwner(tenantId, customerId,
                ownerId, ownerName, ownerDeptId, customer.getCollaboratorIds(),
                operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，分配失败");
        }

        // 插入 PRIMARY 记录
        CrmCustomerOwner newPrimary = new CrmCustomerOwner();
        newPrimary.setId(idGenerator.nextId());
        newPrimary.setTenantId(tenantId);
        newPrimary.setCustomerId(customerId);
        newPrimary.setUserId(ownerId);
        newPrimary.setUserName(ownerName);
        newPrimary.setRoleType(OwnerRoleType.PRIMARY.name());
        newPrimary.setStatus("ACTIVE");
        newPrimary.setCreateBy(operatorName);
        newPrimary.setUpdateBy(operatorName);
        newPrimary.setVersion(0);
        newPrimary.setDelFlag("0");
        ownerMapper.insert(newPrimary);

        // 记录变更
        CrmOwnerChange change = new CrmOwnerChange();
        change.setId(idGenerator.nextId());
        change.setTenantId(tenantId);
        change.setCustomerId(customerId);
        change.setChangeType(OwnerChangeType.ASSIGN.name());
        change.setTargetPrimaryOwnerId(ownerId);
        change.setTargetPrimaryOwnerName(ownerName);
        change.setOperatorId(operatorId);
        change.setOperatorName(operatorName);
        changeMapper.insert(change);

        return change;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmOwnerChange addCollaborator(Long customerId, Long collaboratorId, String collaboratorName)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 查询客户
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_ASSIGN);
        permissionService.check(ctx);

        // 检查是否已是协同人
        List<CrmCustomerOwner> existingCollabs = ownerMapper.selectActiveCollaborators(tenantId, customerId);
        for (CrmCustomerOwner c : existingCollabs)
        {
            if (collaboratorId.equals(c.getUserId()))
            {
                throw new IllegalStateException("该用户已是此客户的协同人");
            }
        }

        // 插入协同人关系
        CrmCustomerOwner collab = new CrmCustomerOwner();
        collab.setId(idGenerator.nextId());
        collab.setTenantId(tenantId);
        collab.setCustomerId(customerId);
        collab.setUserId(collaboratorId);
        collab.setUserName(collaboratorName);
        collab.setRoleType(OwnerRoleType.COLLABORATOR.name());
        collab.setStatus("ACTIVE");
        collab.setCreateBy(operatorName);
        collab.setUpdateBy(operatorName);
        collab.setVersion(0);
        collab.setDelFlag("0");
        ownerMapper.insert(collab);

        // 更新客户 collaboratorIds
        String newCollaboratorIds = addCollaboratorId(customer.getCollaboratorIds(), collaboratorId);
        customerMapper.updatePrimaryOwner(tenantId, customerId,
                customer.getPrimaryOwnerId(), customer.getPrimaryOwnerName(),
                customer.getOwnerDeptId(), newCollaboratorIds,
                operatorName, customer.getVersion());

        // 记录变更
        CrmOwnerChange change = new CrmOwnerChange();
        change.setId(idGenerator.nextId());
        change.setTenantId(tenantId);
        change.setCustomerId(customerId);
        change.setChangeType(OwnerChangeType.COLLABORATOR_ADD.name());
        change.setAddedCollaboratorIds(String.valueOf(collaboratorId));
        change.setOperatorId(operatorId);
        change.setOperatorName(operatorName);
        changeMapper.insert(change);

        recordAudit(tenantId, customerId, operatorId, operatorName,
                "COLLABORATOR_ADD", null, collaboratorName);
        recordTimeline(tenantId, customerId, operatorId, operatorName,
                "COLLABORATOR_ADDED", collaboratorName);

        log.info("Collaborator added: tenantId={}, customerId={}, collaborator={}, operator={}",
                tenantId, customerId, collaboratorName, operatorName);

        return change;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmOwnerChange removeCollaborator(Long customerId, Long collaboratorId)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 查询客户
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_ASSIGN);
        permissionService.check(ctx);

        // 停用协同人关系
        int rows = ownerMapper.deactivateCollaborator(tenantId, customerId, collaboratorId, operatorName);
        if (rows == 0)
        {
            throw new IllegalArgumentException("协同人不存在或已停用");
        }

        // 更新客户 collaboratorIds
        String newCollaboratorIds = removeCollaboratorId(customer.getCollaboratorIds(), collaboratorId);
        customerMapper.updatePrimaryOwner(tenantId, customerId,
                customer.getPrimaryOwnerId(), customer.getPrimaryOwnerName(),
                customer.getOwnerDeptId(), newCollaboratorIds,
                operatorName, customer.getVersion());

        // 记录变更
        CrmOwnerChange change = new CrmOwnerChange();
        change.setId(idGenerator.nextId());
        change.setTenantId(tenantId);
        change.setCustomerId(customerId);
        change.setChangeType(OwnerChangeType.COLLABORATOR_REMOVE.name());
        change.setRemovedCollaboratorIds(String.valueOf(collaboratorId));
        change.setOperatorId(operatorId);
        change.setOperatorName(operatorName);
        changeMapper.insert(change);

        recordAudit(tenantId, customerId, operatorId, operatorName,
                "COLLABORATOR_REMOVE", null, String.valueOf(collaboratorId));
        recordTimeline(tenantId, customerId, operatorId, operatorName,
                "COLLABORATOR_REMOVED", String.valueOf(collaboratorId));

        log.info("Collaborator removed: tenantId={}, customerId={}, collaboratorId={}, operator={}",
                tenantId, customerId, collaboratorId, operatorName);

        return change;
    }

    @Override
    public List<CrmCustomerOwner> listMembers(Long customerId)
    {
        String tenantId = TenantContext.getTenantId();
        return ownerMapper.selectActiveByCustomer(tenantId, customerId);
    }

    @Override
    public List<CrmOwnerChange> listChangeHistory(Long customerId)
    {
        String tenantId = TenantContext.getTenantId();
        return changeMapper.selectByCustomer(tenantId, customerId);
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

    /**
     * 向协同人 ID 列表中添加一个 ID
     */
    private String addCollaboratorId(String collaboratorIds, Long userId)
    {
        if (collaboratorIds == null || collaboratorIds.isEmpty())
        {
            return String.valueOf(userId);
        }
        // 检查是否已存在
        String[] ids = collaboratorIds.split(",");
        for (String id : ids)
        {
            if (id.trim().equals(String.valueOf(userId)))
            {
                return collaboratorIds; // 已存在
            }
        }
        return collaboratorIds + "," + userId;
    }

    /**
     * 从协同人 ID 列表中移除一个 ID
     */
    private String removeCollaboratorId(String collaboratorIds, Long userId)
    {
        if (collaboratorIds == null || collaboratorIds.isEmpty())
        {
            return collaboratorIds;
        }
        List<String> result = new ArrayList<>();
        for (String id : collaboratorIds.split(","))
        {
            if (!id.trim().equals(String.valueOf(userId)))
            {
                result.add(id.trim());
            }
        }
        return result.isEmpty() ? null : String.join(",", result);
    }

    private void recordAudit(String tenantId, Long customerId, Long operatorId,
                             String operatorName, String action, String beforeData, String afterData)
    {
        CrmAuditEvent event = new CrmAuditEvent();
        event.setTenantId(tenantId);
        event.setEventType("CUSTOMER_OWNER");
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
