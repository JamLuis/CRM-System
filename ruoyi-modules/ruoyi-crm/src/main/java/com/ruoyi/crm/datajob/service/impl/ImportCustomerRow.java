package com.ruoyi.crm.datajob.service.impl;

import com.ruoyi.common.core.annotation.Excel;

import java.io.Serializable;

/**
 * 客户导入行（Excel 解析 DTO）
 * <p>
 * 列与导入模板一致：客户名称*、省、市、区/县、详细地址、重要程度、客户来源、行业、备注、下次跟进时间*
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

    @Excel(name = "重要程度")
    private String importance;

    @Excel(name = "客户来源")
    private String source;

    @Excel(name = "行业")
    private String industry;

    @Excel(name = "备注")
    private String remark;

    @Excel(name = "下次跟进时间")
    private String nextFollowUpAt;

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

    public String getNextFollowUpAt()
    {
        return nextFollowUpAt;
    }

    public void setNextFollowUpAt(String nextFollowUpAt)
    {
        this.nextFollowUpAt = nextFollowUpAt;
    }
}
