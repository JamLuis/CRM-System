package com.ruoyi.crm.permission;

import com.ruoyi.crm.customer.domain.CrmCustomer;

public interface CustomerAccessGuard
{
    CrmCustomer check(Long customerId, PermissionCode permissionCode);
}
