package com.ruoyi.crm.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据范围类型枚举测试
 */
@DisplayName("数据范围类型枚举测试")
class ScopeTypeTest
{
    @Test
    @DisplayName("fromString 正确解析已知范围类型")
    void testFromStringKnown()
    {
        assertEquals(ScopeType.ALL, ScopeType.fromString("ALL"));
        assertEquals(ScopeType.DEPT, ScopeType.fromString("DEPT"));
        assertEquals(ScopeType.SELF_CREATED_OR_MEMBER, ScopeType.fromString("SELF_CREATED_OR_MEMBER"));
    }

    @Test
    @DisplayName("fromString 对 null 返回默认值 SELF_CREATED_OR_MEMBER")
    void testFromStringNull()
    {
        assertEquals(ScopeType.SELF_CREATED_OR_MEMBER, ScopeType.fromString(null));
    }

    @Test
    @DisplayName("fromString 对未知值返回默认值 SELF_CREATED_OR_MEMBER")
    void testFromStringUnknown()
    {
        assertEquals(ScopeType.SELF_CREATED_OR_MEMBER, ScopeType.fromString("UNKNOWN"));
        assertEquals(ScopeType.SELF_CREATED_OR_MEMBER, ScopeType.fromString(""));
    }
}
