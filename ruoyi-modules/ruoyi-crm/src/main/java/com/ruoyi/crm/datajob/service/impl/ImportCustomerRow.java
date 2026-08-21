package com.ruoyi.crm.datajob.service.impl;

import com.ruoyi.common.core.annotation.Excel;

import java.io.Serializable;
import java.util.Date;

/**
 * 客户导入行（Excel 解析 DTO）
 * <p>
 * 列与导入模板一致：客户名称*、省、市、区/县、详细地址、重要程度、客户来源、行业、备注、下次跟进时间。
 * 同时兼容历史钉钉表格中的“地址”“客户行业”列。
 *
 * @author ruoyi-crm
 */
public class ImportCustomerRow implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "客户名称")
    private String name;

    @Excel(name = "省")
    private String addressProvince;

    @Excel(name = "市")
    private String addressCity;

    @Excel(name = "区/县")
    private String addressDistrict;

    @Excel(name = "详细地址")
    private String addressDetail;

    @Excel(name = "地址")
    private String legacyAddress;

    @Excel(name = "客户标签")
    private String tags;

    @Excel(name = "跟进力度")
    private String followUpIntensity;

    @Excel(name = "级别")
    private String legacyLevel;

    @Excel(name = "客户跟进状态")
    private String sourceFollowUpStatus;

    @Excel(name = "客户群")
    private String customerGroup;

    @Excel(name = "客户状态")
    private String sourceCustomerStatus;

    @Excel(name = "重要程度")
    private String importance;

    @Excel(name = "客户来源")
    private String source;

    @Excel(name = "介绍客户名称")
    private String referredCustomerName;

    @Excel(name = "客户来源（其他）")
    private String sourceOther;

    @Excel(name = "行业")
    private String industry;

    @Excel(name = "客户行业")
    private String legacyIndustry;

    @Excel(name = "客户行业（其他）")
    private String industryOther;

    @Excel(name = "备注")
    private String remark;

    @Excel(name = "下次跟进时间")
    private String nextFollowUpAt;

    @Excel(name = "创建者")
    private String sourceCreatorName;

    @Excel(name = "负责人")
    private String sourceOwnerName;

    @Excel(name = "协同人")
    private String sourceCollaboratorNames;

    @Excel(name = "创建时间")
    private Date sourceCreateTime;

    @Excel(name = "更新时间")
    private Date sourceUpdateTime;

    @Excel(name = "掉保时间")
    private Date droppedProtectionAt;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public String getAddressDetail()
    {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail)
    {
        this.addressDetail = addressDetail;
    }

    public String getLegacyAddress()
    {
        return legacyAddress;
    }

    public void setLegacyAddress(String legacyAddress)
    {
        this.legacyAddress = legacyAddress;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getFollowUpIntensity()
    {
        return followUpIntensity;
    }

    public void setFollowUpIntensity(String followUpIntensity)
    {
        this.followUpIntensity = followUpIntensity;
    }

    public String getLegacyLevel()
    {
        return legacyLevel;
    }

    public void setLegacyLevel(String legacyLevel)
    {
        this.legacyLevel = legacyLevel;
    }

    public String getSourceFollowUpStatus()
    {
        return sourceFollowUpStatus;
    }

    public void setSourceFollowUpStatus(String sourceFollowUpStatus)
    {
        this.sourceFollowUpStatus = sourceFollowUpStatus;
    }

    public String getCustomerGroup()
    {
        return customerGroup;
    }

    public void setCustomerGroup(String customerGroup)
    {
        this.customerGroup = customerGroup;
    }

    public String getSourceCustomerStatus()
    {
        return sourceCustomerStatus;
    }

    public void setSourceCustomerStatus(String sourceCustomerStatus)
    {
        this.sourceCustomerStatus = sourceCustomerStatus;
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

    public String getReferredCustomerName()
    {
        return referredCustomerName;
    }

    public void setReferredCustomerName(String referredCustomerName)
    {
        this.referredCustomerName = referredCustomerName;
    }

    public String getSourceOther()
    {
        return sourceOther;
    }

    public void setSourceOther(String sourceOther)
    {
        this.sourceOther = sourceOther;
    }

    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    public String getLegacyIndustry()
    {
        return legacyIndustry;
    }

    public void setLegacyIndustry(String legacyIndustry)
    {
        this.legacyIndustry = legacyIndustry;
    }

    public String getIndustryOther()
    {
        return industryOther;
    }

    public void setIndustryOther(String industryOther)
    {
        this.industryOther = industryOther;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getNextFollowUpAt()
    {
        return nextFollowUpAt;
    }

    public void setNextFollowUpAt(String nextFollowUpAt)
    {
        this.nextFollowUpAt = nextFollowUpAt;
    }

    public String getSourceCreatorName()
    {
        return sourceCreatorName;
    }

    public void setSourceCreatorName(String sourceCreatorName)
    {
        this.sourceCreatorName = sourceCreatorName;
    }

    public String getSourceOwnerName()
    {
        return sourceOwnerName;
    }

    public void setSourceOwnerName(String sourceOwnerName)
    {
        this.sourceOwnerName = sourceOwnerName;
    }

    public String getSourceCollaboratorNames()
    {
        return sourceCollaboratorNames;
    }

    public void setSourceCollaboratorNames(String sourceCollaboratorNames)
    {
        this.sourceCollaboratorNames = sourceCollaboratorNames;
    }

    public Date getSourceCreateTime()
    {
        return sourceCreateTime;
    }

    public void setSourceCreateTime(Date sourceCreateTime)
    {
        this.sourceCreateTime = sourceCreateTime;
    }

    public Date getSourceUpdateTime()
    {
        return sourceUpdateTime;
    }

    public void setSourceUpdateTime(Date sourceUpdateTime)
    {
        this.sourceUpdateTime = sourceUpdateTime;
    }

    public Date getDroppedProtectionAt()
    {
        return droppedProtectionAt;
    }

    public void setDroppedProtectionAt(Date droppedProtectionAt)
    {
        this.droppedProtectionAt = droppedProtectionAt;
    }
}
