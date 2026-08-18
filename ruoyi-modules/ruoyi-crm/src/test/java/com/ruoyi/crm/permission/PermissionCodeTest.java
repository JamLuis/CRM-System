package com.ruoyi.crm.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限码枚举测试
 */
@DisplayName("权限码枚举测试")
class PermissionCodeTest
{
    @Test
    @DisplayName("权限码格式正确：模块:资源:操作")
    void testPermissionCodeFormat()
    {
        assertEquals("crm:customer:read", PermissionCode.CRM_CUSTOMER_READ.getCode());
        assertEquals("crm:customer:write", PermissionCode.CRM_CUSTOMER_WRITE.getCode());
        assertEquals("crm:customer:assign", PermissionCode.CRM_CUSTOMER_ASSIGN.getCode());
        assertEquals("crm:customer:create", PermissionCode.CRM_CUSTOMER_CREATE.getCode());
        assertEquals("crm:customer:status", PermissionCode.CRM_CUSTOMER_STATUS.getCode());
        assertEquals("crm:customer:export", PermissionCode.CRM_CUSTOMER_EXPORT.getCode());
        assertEquals("crm:contact:read", PermissionCode.CRM_CONTACT_READ.getCode());
        assertEquals("crm:contact:write", PermissionCode.CRM_CONTACT_WRITE.getCode());
        assertEquals("crm:followup:read", PermissionCode.CRM_FOLLOWUP_READ.getCode());
        assertEquals("crm:followup:write", PermissionCode.CRM_FOLLOWUP_WRITE.getCode());
        assertEquals("crm:opportunity:read", PermissionCode.CRM_OPPORTUNITY_READ.getCode());
        assertEquals("crm:opportunity:write", PermissionCode.CRM_OPPORTUNITY_WRITE.getCode());
        assertEquals("crm:audit:query", PermissionCode.CRM_AUDIT_QUERY.getCode());
        assertEquals("crm:admin:grant", PermissionCode.CRM_ADMIN_GRANT.getCode());
        assertEquals("crm:admin:orgsync", PermissionCode.CRM_ADMIN_ORG_SYNC.getCode());
        assertEquals("crm:admin:*", PermissionCode.CRM_ADMIN_ALL.getCode());
    }

    @Test
    @DisplayName("fromString 正确解析已知权限码")
    void testFromStringKnown()
    {
        assertEquals(PermissionCode.CRM_CUSTOMER_READ, PermissionCode.fromString("crm:customer:read"));
        assertEquals(PermissionCode.CRM_CUSTOMER_WRITE, PermissionCode.fromString("crm:customer:write"));
        assertEquals(PermissionCode.CRM_ADMIN_ALL, PermissionCode.fromString("crm:admin:*"));
    }

    @Test
    @DisplayName("fromString 对未知权限码返回 null")
    void testFromStringUnknown()
    {
        assertNull(PermissionCode.fromString("crm:unknown:action"));
        assertNull(PermissionCode.fromString(null));
    }
}
