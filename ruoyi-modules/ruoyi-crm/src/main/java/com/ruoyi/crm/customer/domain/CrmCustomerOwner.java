package com.ruoyi.crm.customer.domain;

import com.ruoyi.crm.common.domain.CrmBaseEntity;

/**
 * CRM 客户成员关系实体 crm_customer_owner
 * <p>
 * 记录客户的主负责人和协同人关系。
 * 一个客户同时只有一名 ACTIVE 的 PRIMARY 成员。
 *
 * @author ruoyi-crm
 */
public class CrmCustomerOwner extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键（雪花） */
    private Long id;
    /** 客户ID */
    private Long customerId;
    /** 用户ID */
    private Long userId;
    /** 用户姓名快照 */
    private String userName;
    /** 角色类型(PRIMARY/COLLABORATOR) */
    private String roleType;
    /** 状态(ACTIVE/INACTIVE) */
    private String status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getRoleType()
    {
        return roleType;
    }

    public void setRoleType(String roleType)
    {
        this.roleType = roleType;
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
