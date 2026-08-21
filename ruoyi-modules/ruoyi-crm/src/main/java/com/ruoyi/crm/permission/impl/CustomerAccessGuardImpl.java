package com.ruoyi.crm.permission.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.permission.CustomerAccessGuard;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerAccessGuardImpl implements CustomerAccessGuard
{
    @Autowired
    private CrmCustomerMapper customerMapper;

    @Autowired
    private PermissionService permissionService;

    @Override
    public CrmCustomer check(Long customerId, PermissionCode permissionCode)
    {
        String tenantId = TenantContext.getTenantId();
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        Long operatorId = SecurityUtils.getUserId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;

        PermissionContext context = new PermissionContext();
        context.setOperatorId(operatorId);
        context.setOperatorDeptId(operatorDeptId);
        context.setAdmin(SecurityUtils.isAdmin(operatorId));
        context.setPermissionCode(permissionCode);
        context.setPrimaryOwnerId(customer.getPrimaryOwnerId());
        context.setCollaboratorIds(customer.getCollaboratorIds());
        context.setCreatorDeptId(customer.getCreatorDeptId());
        context.setOwnerDeptId(customer.getOwnerDeptId());
        context.setOperatingStatus(customer.getOperatingStatus());
        permissionService.check(context);
        return customer;
    }
}
