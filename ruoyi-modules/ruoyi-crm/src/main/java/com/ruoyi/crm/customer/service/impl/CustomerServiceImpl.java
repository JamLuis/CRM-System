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
import com.ruoyi.crm.customer.service.CustomerService;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionDeniedException;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.crm.permission.ScopeType;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户服务实现
 * <p>
 * 所有写操作在事务内执行，审计和时间线记录在同一事务内追加。
 *
 * @author ruoyi-crm
 */
@Service
public class CustomerServiceImpl implements CustomerService
{
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CrmCustomerMapper customerMapper;

    @Autowired
    private CrmCustomerOwnerMapper ownerMapper;

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
    public CrmCustomer create(CrmCustomer customer)
    {
        String tenantId = TenantContext.getTenantId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 1. 权限校验
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(operatorId);
        ctx.setOperatorDeptId(operatorDeptId);
        ctx.setAdmin(isAdmin);
        ctx.setPermissionCode(PermissionCode.CRM_CUSTOMER_CREATE);
        ctx.setOperatingStatus(OperatingStatus.NORMAL.getValue());
        permissionService.check(ctx);

        // 2. 重名硬查重
        String activeNameKey = normalizeNameKey(customer.getName());
        customer.setActiveNameKey(activeNameKey);
        CrmCustomer existing = customerMapper.selectByActiveNameKey(tenantId, activeNameKey);
        if (existing != null)
        {
            throw new IllegalArgumentException("客户名称已存在：" + customer.getName());
        }

        // 3. 设置默认值
        Long customerId = idGenerator.nextId();
        customer.setCustomerId(customerId);
        customer.setTenantId(tenantId);
        customer.setCustomerCode(generateCustomerCode(customerId));
        customer.setOperatingStatus(OperatingStatus.NORMAL.getValue());
        if (customer.getLifecycleStage() == null || customer.getLifecycleStage().isEmpty())
        {
            customer.setLifecycleStage(LifecycleStage.NEW.getValue());
        }
        // 地址 NOT NULL 字段空串兜底（与导入逻辑保持一致）
        if (customer.getAddressProvince() == null)
        {
            customer.setAddressProvince("");
        }
        if (customer.getAddressCity() == null)
        {
            customer.setAddressCity("");
        }
        if (customer.getAddressDetail() == null)
        {
            customer.setAddressDetail("");
        }
        // 正常客户必须有 nextFollowUpAt
        if (customer.getNextFollowUpAt() == null)
        {
            throw new IllegalArgumentException("正常客户必须设置下次跟进时间");
        }
        customer.setVersion(0);
        customer.setDelFlag("0");
        customer.setCreateBy(operatorName);
        customer.setUpdateBy(operatorName);

        // 设置部门信息
        if (customer.getCreatorDeptId() == null)
        {
            customer.setCreatorDeptId(operatorDeptId);
        }
        if (customer.getOwnerDeptId() == null && customer.getPrimaryOwnerId() != null)
        {
            // 主负责人部门由调用方传入或默认为操作人部门
            customer.setOwnerDeptId(operatorDeptId);
        }

        // 4. 插入客户
        customerMapper.insert(customer);

        // 5. 插入主负责人关系
        if (customer.getPrimaryOwnerId() != null)
        {
            CrmCustomerOwner owner = new CrmCustomerOwner();
            owner.setId(idGenerator.nextId());
            owner.setTenantId(tenantId);
            owner.setCustomerId(customerId);
            owner.setUserId(customer.getPrimaryOwnerId());
            owner.setUserName(customer.getPrimaryOwnerName());
            owner.setRoleType(OwnerRoleType.PRIMARY.name());
            owner.setStatus("ACTIVE");
            owner.setCreateBy(operatorName);
            owner.setUpdateBy(operatorName);
            owner.setVersion(0);
            owner.setDelFlag("0");
            ownerMapper.insert(owner);
        }

        // 6. 记录审计
        recordAudit(tenantId, customerId, operatorId, operatorName, "CREATE", null, customer.getName());

        // 7. 记录时间线
        recordTimeline(tenantId, customerId, operatorId, operatorName, "CUSTOMER_CREATED", customer.getName());

        log.info("Customer created: tenantId={}, customerId={}, name={}, operator={}",
                tenantId, customerId, customer.getName(), operatorName);

        return customer;
    }

    @Override
    public CrmCustomer detail(Long customerId)
    {
        String tenantId = TenantContext.getTenantId();
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 权限校验
        Long operatorId = SecurityUtils.getUserId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_READ);
        permissionService.check(ctx);

        return customer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmCustomer edit(CrmCustomer customer)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 查询现有客户
        CrmCustomer existing = customerMapper.selectByCustomerId(tenantId, customer.getCustomerId());
        if (existing == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customer.getCustomerId());
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(existing, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CUSTOMER_WRITE);
        permissionService.check(ctx);

        // 如果名称变更，需要重新查重
        if (customer.getName() != null && !customer.getName().equals(existing.getName()))
        {
            String newActiveNameKey = normalizeNameKey(customer.getName());
            CrmCustomer dup = customerMapper.selectByActiveNameKey(tenantId, newActiveNameKey);
            if (dup != null && !dup.getCustomerId().equals(existing.getCustomerId()))
            {
                throw new IllegalArgumentException("客户名称已存在：" + customer.getName());
            }
            customer.setActiveNameKey(newActiveNameKey);
        }
        else
        {
            // 名称未变更，不更新 active_name_key
            customer.setActiveNameKey(null);
        }

        // 保留不可修改字段
        customer.setTenantId(tenantId);
        customer.setCustomerCode(existing.getCustomerCode());
        customer.setOperatingStatus(existing.getOperatingStatus());
        customer.setPrimaryOwnerId(existing.getPrimaryOwnerId());
        customer.setPrimaryOwnerName(existing.getPrimaryOwnerName());
        customer.setCollaboratorIds(existing.getCollaboratorIds());
        customer.setCreatorDeptId(existing.getCreatorDeptId());
        customer.setOwnerDeptId(existing.getOwnerDeptId());
        customer.setLastEffectiveFollowUpAt(existing.getLastEffectiveFollowUpAt());
        customer.setArchivedAt(existing.getArchivedAt());
        customer.setUpdateBy(operatorName);

        // 乐观锁：使用传入的 version
        if (customer.getVersion() == null)
        {
            customer.setVersion(existing.getVersion());
        }

        int rows = customerMapper.update(customer);
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }

        // 记录审计
        recordAudit(tenantId, customer.getCustomerId(), operatorId, operatorName,
                "UPDATE", existing.getName(), customer.getName());

        log.info("Customer updated: tenantId={}, customerId={}, operator={}",
                tenantId, customer.getCustomerId(), operatorName);

        return customerMapper.selectByCustomerId(tenantId, customer.getCustomerId());
    }

    @Override
    public List<CrmCustomer> list(CrmCustomer query)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        ScopeType scopeType = permissionService.getScopeType(tenantId, operatorId);

        return customerMapper.selectVisibleList(
                tenantId, query, scopeType.name(), operatorId, operatorDeptId);
    }

    @Override
    public List<CrmCustomer> listAll(CrmCustomer query)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        if (!isAdmin)
        {
            throw new PermissionDeniedException("仅管理员可查询全部客户");
        }

        return customerMapper.selectList(tenantId, query);
    }

    // ==================== Private helpers ====================

    /**
     * 规范化客户名称（去空格 + 转小写），用于重名查重
     */
    private String normalizeNameKey(String name)
    {
        if (name == null)
        {
            return "";
        }
        return name.replaceAll("\\s+", "").toLowerCase();
    }

    /**
     * 生成客户编码
     */
    private String generateCustomerCode(Long customerId)
    {
        return "C" + String.format("%08d", customerId % 100000000);
    }

    /**
     * 构建权限上下文
     */
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
     * 记录审计事件
     */
    private void recordAudit(String tenantId, Long customerId, Long operatorId,
                             String operatorName, String action, String beforeData, String afterData)
    {
        CrmAuditEvent event = new CrmAuditEvent();
        event.setTenantId(tenantId);
        event.setEventType("CUSTOMER");
        event.setEntityType("CUSTOMER");
        event.setEntityId(String.valueOf(customerId));
        event.setOperatorId(operatorId);
        event.setOperatorName(operatorName);
        event.setAction(action);
        event.setBeforeData(beforeData);
        event.setAfterData(afterData);
        auditEventService.record(event);
    }

    /**
     * 记录客户时间线
     */
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
