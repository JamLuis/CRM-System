package com.ruoyi.crm.followup.domain;

import java.io.Serializable;

/**
 * CRM 跟进—联系人关系实体 crm_follow_up_contact
 * <p>
 * 纯关联表，不可变。
 *
 * @author ruoyi-crm
 */
public class CrmFollowUpContact implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键（雪花） */
    private Long id;
    /** 租户ID */
    private String tenantId;
    /** 跟进记录ID */
    private Long followUpId;
    /** 联系人ID */
    private Long contactId;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }

    public Long getFollowUpId()
    {
        return followUpId;
    }

    public void setFollowUpId(Long followUpId)
    {
        this.followUpId = followUpId;
    }

    public Long getContactId()
    {
        return contactId;
    }

    public void setContactId(Long contactId)
    {
        this.contactId = contactId;
    }
}
