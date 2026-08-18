package com.ruoyi.crm.permission;

/**
 * 权限范围类型枚举
 * <p>
 * 对应 crm_role_scope.scope_type 字段
 *
 * @author ruoyi-crm
 */
public enum ScopeType
{
    /** 本人主负责或协同的客户 */
    SELF_CREATED_OR_MEMBER,

    /** 本部门及下级部门的客户 */
    DEPT,

    /** 全部客户 */
    ALL;

    /**
     * 从字符串解析范围类型
     */
    public static ScopeType fromString(String value)
    {
        if (value == null)
        {
            return SELF_CREATED_OR_MEMBER;
        }
        for (ScopeType type : values())
        {
            if (type.name().equals(value))
            {
                return type;
            }
        }
        return SELF_CREATED_OR_MEMBER;
    }
}
