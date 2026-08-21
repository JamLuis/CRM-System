package com.ruoyi.crm.dingtalk.service;

import com.ruoyi.crm.tenant.domain.CrmDingtalkDirectoryUser;

import java.util.List;
import com.ruoyi.system.api.domain.SysRole;

public interface CrmAccessGrantService
{
    List<CrmDingtalkDirectoryUser> listDirectoryUsers(String tenantId, String keyword, String accessStatus);

    Long grant(String tenantId, String dingtalkUserId, List<Long> roleIds);

    void revoke(String tenantId, String dingtalkUserId);

    List<SysRole> listAssignableRoles();
}
