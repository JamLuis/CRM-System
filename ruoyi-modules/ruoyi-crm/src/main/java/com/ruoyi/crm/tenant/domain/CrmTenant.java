package com.ruoyi.crm.tenant.domain;

import com.ruoyi.crm.common.domain.CrmBaseEntity;

/**
 * CRM 租户对象 crm_tenant
 *
 * @author ruoyi-crm
 */
public class CrmTenant extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 租户 ID */
    private String tenantId;
    /** 租户名称 */
    private String tenantName;
    /** 状态（0正常 1停用） */
    private String status;

    public String getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getTenantName()
    {
        return tenantName;
    }

    public void setTenantName(String tenantName)
    {
        this.tenantName = tenantName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
