package com.ruoyi.crm.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限拒绝异常测试
 */
@DisplayName("权限拒绝异常测试")
class PermissionDeniedExceptionTest
{
    @Test
    @DisplayName("异常消息和原因正确设置")
    void testExceptionMessage()
    {
        PermissionDeniedException ex = new PermissionDeniedException("权限不足");
        assertEquals("权限不足", ex.getMessage());
        assertEquals("权限不足", ex.getReason());
    }

    @Test
    @DisplayName("带原因的异常正确设置")
    void testExceptionWithCause()
    {
        Throwable cause = new RuntimeException("underlying error");
        PermissionDeniedException ex = new PermissionDeniedException("状态不允许", cause);
        assertEquals("状态不允许", ex.getMessage());
        assertEquals("状态不允许", ex.getReason());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("异常是 RuntimeException 子类")
    void testIsRuntimeException()
    {
        PermissionDeniedException ex = new PermissionDeniedException("test");
        assertTrue(ex instanceof RuntimeException);
    }
}
