package com.ruoyi.crm.customer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

import java.util.Date;

/**
 * CRM 客户实体 crm_customer
 * <p>
 * 客户是 CRM 的核心业务对象。每个客户有唯一主负责人、零到多名协同人，
 * 以及独立的经营状态和生命周期阶段。
 *
 * @author ruoyi-crm
 */
public class CrmCustomer extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 客户ID（雪花） */
    private Long customerId;
    /** 客户编码（系统生成，不可修改） */
    private String customerCode;
    /** 客户名称 */
    private String name;
    /** 规范化名称（去空格小写），用于重名校验 */
    private String activeNameKey;
    /** 省/直辖市 */
    private String addressProvince;
    /** 市/区县 */
    private String addressCity;
    /** 区/县 */
    private String addressDistrict;
    /** 街道/园区 */
    private String addressStreet;
    /** 详细地址 */
    private String addressDetail;
    /** 客户标签（JSON数组） */
    private String tags;
    /** 生命周期阶段 */
    private String lifecycleStage;
    /** 经营状态(正常/暂停跟进/已失效/已归档) */
    private String operatingStatus;
    /** 阶段降级原因 */
    private String stageChangeReason;
    /** 状态变更原因 */
    private String statusChangeReason;
    /** 暂停时计划恢复时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date plannedResumeAt;
    /** 重要程度(一般/重要/非常重要) */
    private String importance;
    /** 客户来源 */
    private String source;
    /** 行业 */
    private String industry;
    /** 备注 */
    private String remark;
    /** 当前主负责人用户ID */
    private Long primaryOwnerId;
    /** 主负责人姓名快照 */
    private String primaryOwnerName;
    /** 协同人ID列表（逗号分隔） */
    private String collaboratorIds;
    /** 创建时部门ID */
    private Long creatorDeptId;
    /** 当前主负责人部门ID */
    private Long ownerDeptId;
    /** 下一次跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextFollowUpAt;
    /** 最近有效跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastEffectiveFollowUpAt;
    /** 归档时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archivedAt;

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getCustomerCode()
    {
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getActiveNameKey()
    {
        return activeNameKey;
    }

    public void setActiveNameKey(String activeNameKey)
    {
        this.activeNameKey = activeNameKey;
    }

    public String getAddressProvince()
    {
        return addressProvince;
    }

    public void setAddressProvince(String addressProvince)
    {
        this.addressProvince = addressProvince;
    }

    public String getAddressCity()
    {
        return addressCity;
    }

    public void setAddressCity(String addressCity)
    {
        this.addressCity = addressCity;
    }

    public String getAddressDistrict()
    {
        return addressDistrict;
    }

    public void setAddressDistrict(String addressDistrict)
    {
        this.addressDistrict = addressDistrict;
    }

    public String getAddressStreet()
    {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet)
    {
        this.addressStreet = addressStreet;
    }

    public String getAddressDetail()
    {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail)
    {
        this.addressDetail = addressDetail;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getLifecycleStage()
    {
        return lifecycleStage;
    }

    public void setLifecycleStage(String lifecycleStage)
    {
        this.lifecycleStage = lifecycleStage;
    }

    public String getOperatingStatus()
    {
        return operatingStatus;
    }

    public void setOperatingStatus(String operatingStatus)
    {
        this.operatingStatus = operatingStatus;
    }

    public String getStageChangeReason()
    {
        return stageChangeReason;
    }

    public void setStageChangeReason(String stageChangeReason)
    {
        this.stageChangeReason = stageChangeReason;
    }

    public String getStatusChangeReason()
    {
        return statusChangeReason;
    }

    public void setStatusChangeReason(String statusChangeReason)
    {
        this.statusChangeReason = statusChangeReason;
    }

    public Date getPlannedResumeAt()
    {
        return plannedResumeAt;
    }

    public void setPlannedResumeAt(Date plannedResumeAt)
    {
        this.plannedResumeAt = plannedResumeAt;
    }

    public String getImportance()
    {
        return importance;
    }

    public void setImportance(String importance)
    {
        this.importance = importance;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Long getPrimaryOwnerId()
    {
        return primaryOwnerId;
    }

    public void setPrimaryOwnerId(Long primaryOwnerId)
    {
        this.primaryOwnerId = primaryOwnerId;
    }

    public String getPrimaryOwnerName()
    {
        return primaryOwnerName;
    }

    public void setPrimaryOwnerName(String primaryOwnerName)
    {
        this.primaryOwnerName = primaryOwnerName;
    }

    public String getCollaboratorIds()
    {
        return collaboratorIds;
    }

    public void setCollaboratorIds(String collaboratorIds)
    {
        this.collaboratorIds = collaboratorIds;
    }

    public Long getCreatorDeptId()
    {
        return creatorDeptId;
    }

    public void setCreatorDeptId(Long creatorDeptId)
    {
        this.creatorDeptId = creatorDeptId;
    }

    public Long getOwnerDeptId()
    {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long ownerDeptId)
    {
        this.ownerDeptId = ownerDeptId;
    }

    public Date getNextFollowUpAt()
    {
        return nextFollowUpAt;
    }

    public void setNextFollowUpAt(Date nextFollowUpAt)
    {
        this.nextFollowUpAt = nextFollowUpAt;
    }

    public Date getLastEffectiveFollowUpAt()
    {
        return lastEffectiveFollowUpAt;
    }

    public void setLastEffectiveFollowUpAt(Date lastEffectiveFollowUpAt)
    {
        this.lastEffectiveFollowUpAt = lastEffectiveFollowUpAt;
    }

    public Date getArchivedAt()
    {
        return archivedAt;
    }

    public void setArchivedAt(Date archivedAt)
    {
        this.archivedAt = archivedAt;
    }
}
