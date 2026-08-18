package com.ruoyi.crm.followup.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 跟进方式枚举测试
 */
@DisplayName("跟进方式枚举测试")
class FollowUpMethodTest
{
    @Test
    @DisplayName("fromString 正确解析已知值")
    void testFromStringKnown()
    {
        assertEquals(FollowUpMethod.PHONE, FollowUpMethod.fromString("电话"));
        assertEquals(FollowUpMethod.WECHAT, FollowUpMethod.fromString("微信"));
        assertEquals(FollowUpMethod.IN_PERSON, FollowUpMethod.fromString("面谈"));
    }

    @Test
    @DisplayName("fromString 对未知值抛出异常")
    void testFromStringUnknown()
    {
        assertThrows(IllegalArgumentException.class, () -> FollowUpMethod.fromString("未知"));
        assertThrows(IllegalArgumentException.class, () -> FollowUpMethod.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> FollowUpMethod.fromString(""));
    }
}
