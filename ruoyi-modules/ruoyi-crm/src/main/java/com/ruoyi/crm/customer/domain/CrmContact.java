package com.ruoyi.crm.customer.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

/**
 * CRM 客户联系人实体 crm_contact
 *
 * @author ruoyi-crm
 */
public class CrmContact extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 联系人ID（雪花） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long contactId;
    /** 外部来源数据 ID（用于幂等导入） */
    private String sourceDataId;
    /** 客户ID */
    private Long customerId;
    /** 联系人姓名 */
    private String name;
    /** 电话类型(手机/座机/其他) */
    private String phoneType;
    /** 国际区号 */
    private String countryCode;
    /** 规范化号码 */
    private String phoneNumber;
    /** 脱敏号码（展示用） */
    private String phoneMasked;
    /** 邮箱 */
    private String email;
    /** 脱敏邮箱 */
    private String emailMasked;
    /** 微信号 */
    private String wechatId;
    /** 脱敏微信号 */
    private String wechatMasked;
    /** 分管内容 */
    private String responsibility;
    /** 职位 */
    private String title;
    /** 是否决策人 */
    private Boolean isDecisionMaker;
    /** 备注 */
    private String remark;
    /** 状态(有效/已停用) */
    private String status;
    /** 导入源负责人名称 */
    private String sourceOwnerNames;
    /** 导入源协同人名称 */
    private String sourceCollaboratorNames;

    public Long getContactId()
    {
        return contactId;
    }

    public void setContactId(Long contactId)
    {
        this.contactId = contactId;
    }

    public String getSourceDataId()
    {
        return sourceDataId;
    }

    public void setSourceDataId(String sourceDataId)
    {
        this.sourceDataId = sourceDataId;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhoneType()
    {
        return phoneType;
    }

    public void setPhoneType(String phoneType)
    {
        this.phoneType = phoneType;
    }

    public String getCountryCode()
    {
        return countryCode;
    }

    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneMasked()
    {
        return phoneMasked;
    }

    public void setPhoneMasked(String phoneMasked)
    {
        this.phoneMasked = phoneMasked;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getEmailMasked()
    {
        return emailMasked;
    }

    public void setEmailMasked(String emailMasked)
    {
        this.emailMasked = emailMasked;
    }

    public String getWechatId()
    {
        return wechatId;
    }

    public void setWechatId(String wechatId)
    {
        this.wechatId = wechatId;
    }

    public String getWechatMasked()
    {
        return wechatMasked;
    }

    public void setWechatMasked(String wechatMasked)
    {
        this.wechatMasked = wechatMasked;
    }

    public String getResponsibility()
    {
        return responsibility;
    }

    public void setResponsibility(String responsibility)
    {
        this.responsibility = responsibility;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Boolean getIsDecisionMaker()
    {
        return isDecisionMaker;
    }

    public void setIsDecisionMaker(Boolean isDecisionMaker)
    {
        this.isDecisionMaker = isDecisionMaker;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getSourceOwnerNames()
    {
        return sourceOwnerNames;
    }

    public void setSourceOwnerNames(String sourceOwnerNames)
    {
        this.sourceOwnerNames = sourceOwnerNames;
    }

    public String getSourceCollaboratorNames()
    {
        return sourceCollaboratorNames;
    }

    public void setSourceCollaboratorNames(String sourceCollaboratorNames)
    {
        this.sourceCollaboratorNames = sourceCollaboratorNames;
    }
}
