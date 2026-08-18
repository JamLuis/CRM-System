package com.ruoyi.crm.tenant.domain;

import com.ruoyi.crm.common.domain.CrmImmutableEntity;

/**
 * 角色数据范围对象 crm_role_scope
 *
 * @author ruoyi-crm
 */
public class CrmRoleScope extends CrmImmutableEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;
    /** 角色 ID */
    private Long roleId;
    /** 范围类型（ALL/DEPT/SELF_CREATED_OR_MEMBER） */
    private String scopeType;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public String getScopeType()
    {
        return scopeType;
    }

    public void setScopeType(String scopeType)
    {
        this.scopeType = scopeType;
    }
}
