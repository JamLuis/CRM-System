package com.ruoyi.crm.permission;

/**
 * CRM 权限操作码
 * <p>
 * 权限码格式：模块:资源:操作
 * 例如：crm:customer:read, crm:customer:write, crm:customer:assign
 *
 * @author ruoyi-crm
 */
public enum PermissionCode
{
    // --- 客户 ---
    /** 查看客户 */
    CRM_CUSTOMER_READ("crm:customer:read"),
    /** 编辑客户核心字段 */
    CRM_CUSTOMER_WRITE("crm:customer:write"),
    /** 分配/移交主负责人 */
    CRM_CUSTOMER_ASSIGN("crm:customer:assign"),
    /** 新建客户 */
    CRM_CUSTOMER_CREATE("crm:customer:create"),
    /** 暂停/恢复/失效/归档客户 */
    CRM_CUSTOMER_STATUS("crm:customer:status"),
    /** 导出客户 */
    CRM_CUSTOMER_EXPORT("crm:customer:export"),

    // --- 联系人 ---
    CRM_CONTACT_READ("crm:contact:read"),
    CRM_CONTACT_WRITE("crm:contact:write"),

    // --- 跟进 ---
    CRM_FOLLOWUP_READ("crm:followup:read"),
    CRM_FOLLOWUP_WRITE("crm:followup:write"),

    // --- 商机 ---
    CRM_OPPORTUNITY_READ("crm:opportunity:read"),
    CRM_OPPORTUNITY_WRITE("crm:opportunity:write"),

    // --- 审计 ---
    CRM_AUDIT_QUERY("crm:audit:query"),

    // --- 管理员 ---
    CRM_ADMIN_GRANT("crm:admin:grant"),
    CRM_ADMIN_ORG_SYNC("crm:admin:orgsync"),
    CRM_ADMIN_ALL("crm:admin:*");

    private final String code;

    PermissionCode(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    /**
     * 从字符串解析权限码
     */
    public static PermissionCode fromString(String code)
    {
        for (PermissionCode pc : values())
        {
            if (pc.code.equals(code))
            {
                return pc;
            }
        }
        return null;
    }
}
