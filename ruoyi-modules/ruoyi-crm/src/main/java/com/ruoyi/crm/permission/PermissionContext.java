package com.ruoyi.crm.permission;

/**
 * 权限检查上下文
 * <p>
 * 封装一次权限检查所需的全部信息：
 * <ul>
 *   <li>操作人 ID、部门 ID、是否管理员</li>
 *   <li>请求的权限码</li>
 *   <li>目标客户的主负责人 ID、协同人 ID 列表、创建部门 ID、主负责人部门 ID、经营状态</li>
 * </ul>
 *
 * @author ruoyi-crm
 */
public class PermissionContext
{
    /** 操作人 ID */
    private Long operatorId;
    /** 操作人部门 ID */
    private Long operatorDeptId;
    /** 操作人是否系统管理员 */
    private boolean admin;
    /** 请求的权限码 */
    private PermissionCode permissionCode;

    // --- 目标客户信息 ---
    /** 客户主负责人 ID */
    private Long primaryOwnerId;
    /** 客户协同人 ID 列表（逗号分隔） */
    private String collaboratorIds;
    /** 客户创建部门 ID */
    private Long creatorDeptId;
    /** 客户当前主负责人部门 ID */
    private Long ownerDeptId;
    /** 客户经营状态（正常/暂停跟进/已失效/已归档） */
    private String operatingStatus;

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public Long getOperatorDeptId()
    {
        return operatorDeptId;
    }

    public void setOperatorDeptId(Long operatorDeptId)
    {
        this.operatorDeptId = operatorDeptId;
    }

    public boolean isAdmin()
    {
        return admin;
    }

    public void setAdmin(boolean admin)
    {
        this.admin = admin;
    }

    public PermissionCode getPermissionCode()
    {
        return permissionCode;
    }

    public void setPermissionCode(PermissionCode permissionCode)
    {
        this.permissionCode = permissionCode;
    }

    public Long getPrimaryOwnerId()
    {
        return primaryOwnerId;
    }

    public void setPrimaryOwnerId(Long primaryOwnerId)
    {
        this.primaryOwnerId = primaryOwnerId;
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

    public String getOperatingStatus()
    {
        return operatingStatus;
    }

    public void setOperatingStatus(String operatingStatus)
    {
        this.operatingStatus = operatingStatus;
    }

    /**
     * 检查操作人是否为该客户的主负责人
     */
    public boolean isPrimaryOwner()
    {
        return operatorId != null && operatorId.equals(primaryOwnerId);
    }

    /**
     * 检查操作人是否为该客户的协同人
     */
    public boolean isCollaborator()
    {
        if (collaboratorIds == null || collaboratorIds.isEmpty() || operatorId == null)
        {
            return false;
        }
        String[] ids = collaboratorIds.split(",");
        for (String id : ids)
        {
            if (operatorId.toString().equals(id.trim()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查操作人部门是否与客户主负责人部门相同
     */
    public boolean isSameDept()
    {
        return operatorDeptId != null && operatorDeptId.equals(ownerDeptId);
    }
}
