package com.ruoyi.crm.datajob.service.impl;

/**
 * 钉钉联系人导出行。
 */
public class ImportContactRow
{
    private String sourceDataId;
    private String customerName;
    private String name;
    private String title;
    private String responsibility;
    private String decisionMaker;
    private String phone;
    private String wechatId;
    private String email;
    private String remark;
    private String sourceCreateTime;
    private String sourceCreatorName;
    private String sourceUpdateTime;
    private String sourceOwnerNames;
    private String sourceCollaboratorNames;

    public String getSourceDataId() { return sourceDataId; }
    public void setSourceDataId(String sourceDataId) { this.sourceDataId = sourceDataId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getResponsibility() { return responsibility; }
    public void setResponsibility(String responsibility) { this.responsibility = responsibility; }
    public String getDecisionMaker() { return decisionMaker; }
    public void setDecisionMaker(String decisionMaker) { this.decisionMaker = decisionMaker; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWechatId() { return wechatId; }
    public void setWechatId(String wechatId) { this.wechatId = wechatId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getSourceCreateTime() { return sourceCreateTime; }
    public void setSourceCreateTime(String sourceCreateTime) { this.sourceCreateTime = sourceCreateTime; }
    public String getSourceCreatorName() { return sourceCreatorName; }
    public void setSourceCreatorName(String sourceCreatorName) { this.sourceCreatorName = sourceCreatorName; }
    public String getSourceUpdateTime() { return sourceUpdateTime; }
    public void setSourceUpdateTime(String sourceUpdateTime) { this.sourceUpdateTime = sourceUpdateTime; }
    public String getSourceOwnerNames() { return sourceOwnerNames; }
    public void setSourceOwnerNames(String sourceOwnerNames) { this.sourceOwnerNames = sourceOwnerNames; }
    public String getSourceCollaboratorNames() { return sourceCollaboratorNames; }
    public void setSourceCollaboratorNames(String sourceCollaboratorNames) { this.sourceCollaboratorNames = sourceCollaboratorNames; }
}
