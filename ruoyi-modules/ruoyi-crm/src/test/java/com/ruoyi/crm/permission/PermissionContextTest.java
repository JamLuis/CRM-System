package com.ruoyi.crm.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限上下文测试
 */
@DisplayName("权限上下文测试")
class PermissionContextTest
{
    @Test
    @DisplayName("操作人是主负责人时 isPrimaryOwner 返回 true")
    void testIsPrimaryOwner()
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(100L);
        ctx.setPrimaryOwnerId(100L);
        assertTrue(ctx.isPrimaryOwner());
    }

    @Test
    @DisplayName("操作人不是主负责人时 isPrimaryOwner 返回 false")
    void testIsNotPrimaryOwner()
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(100L);
        ctx.setPrimaryOwnerId(200L);
        assertFalse(ctx.isPrimaryOwner());
    }

    @Test
    @DisplayName("操作人在协同人列表中时 isCollaborator 返回 true")
    void testIsCollaborator()
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(101L);
        ctx.setCollaboratorIds("101,102,103");
        assertTrue(ctx.isCollaborator());
    }

    @Test
    @DisplayName("操作人不在协同人列表中时 isCollaborator 返回 false")
    void testIsNotCollaborator()
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(999L);
        ctx.setCollaboratorIds("101,102,103");
        assertFalse(ctx.isCollaborator());
    }

    @Test
    @DisplayName("协同人列表为空时 isCollaborator 返回 false")
    void testEmptyCollaboratorIds()
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(101L);
        ctx.setCollaboratorIds(null);
        assertFalse(ctx.isCollaborator());

        ctx.setCollaboratorIds("");
        assertFalse(ctx.isCollaborator());
    }

    @Test
    @DisplayName("操作人部门与客户主负责人部门相同时 isSameDept 返回 true")
    void testIsSameDept()
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorDeptId(10L);
        ctx.setOwnerDeptId(10L);
        assertTrue(ctx.isSameDept());
    }

    @Test
    @DisplayName("操作人部门与客户主负责人部门不同时 isSameDept 返回 false")
    void testIsNotSameDept()
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorDeptId(10L);
        ctx.setOwnerDeptId(20L);
        assertFalse(ctx.isSameDept());
    }

    @Test
    @DisplayName("协同人 ID 带空格时也能正确匹配")
    void testCollaboratorWithSpaces()
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(101L);
        ctx.setCollaboratorIds("101, 102, 103");
        assertTrue(ctx.isCollaborator());
    }
}
