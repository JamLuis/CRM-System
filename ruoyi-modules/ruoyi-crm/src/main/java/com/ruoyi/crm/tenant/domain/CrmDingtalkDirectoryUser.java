package com.ruoyi.crm.tenant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

import java.util.Date;

/**
 * 钉钉企业通讯录人员快照。
 *
 * 组织同步只维护本表，不代表人员已经取得 CRM 访问权。
 */
public class CrmDingtalkDirectoryUser extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String dingtalkUserId;
    @JsonIgnore
    private String unionId;
    private String name;
    private String mobile;
    private String email;
    private String title;
    private String deptIds;
    private String deptNames;
    private Long sysDeptId;
    private Boolean active;
    private Date lastSyncTime;

    /** 以下字段由授权列表联表查询填充。 */
    private Boolean accessGranted;
    private Long sysUserId;
    private String roleIds;
    private String roleNames;
    private String permissionCodes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDingtalkUserId() { return dingtalkUserId; }
    public void setDingtalkUserId(String dingtalkUserId) { this.dingtalkUserId = dingtalkUserId; }
    public String getUnionId() { return unionId; }
    public void setUnionId(String unionId) { this.unionId = unionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDeptIds() { return deptIds; }
    public void setDeptIds(String deptIds) { this.deptIds = deptIds; }
    public String getDeptNames() { return deptNames; }
    public void setDeptNames(String deptNames) { this.deptNames = deptNames; }
    public Long getSysDeptId() { return sysDeptId; }
    public void setSysDeptId(Long sysDeptId) { this.sysDeptId = sysDeptId; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Date getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(Date lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    public Boolean getAccessGranted() { return accessGranted; }
    public void setAccessGranted(Boolean accessGranted) { this.accessGranted = accessGranted; }
    public Long getSysUserId() { return sysUserId; }
    public void setSysUserId(Long sysUserId) { this.sysUserId = sysUserId; }
    public String getRoleIds() { return roleIds; }
    public void setRoleIds(String roleIds) { this.roleIds = roleIds; }
    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }
    public String getPermissionCodes() { return permissionCodes; }
    public void setPermissionCodes(String permissionCodes) { this.permissionCodes = permissionCodes; }
}
