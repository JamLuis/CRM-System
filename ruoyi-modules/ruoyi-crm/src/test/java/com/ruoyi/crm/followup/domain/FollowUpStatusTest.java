package com.ruoyi.crm.followup.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 跟进状态（健康度）枚举测试
 */
@DisplayName("跟进状态（健康度）枚举测试")
class FollowUpStatusTest
{
    @Test
    @DisplayName("fromString 正确解析已知值（按 name 匹配）")
    void testFromStringKnown()
    {
        assertEquals(FollowUpStatus.NORMAL, FollowUpStatus.fromString("NORMAL"));
        assertEquals(FollowUpStatus.INSUFFICIENT, FollowUpStatus.fromString("INSUFFICIENT"));
        assertEquals(FollowUpStatus.SEVERE_INSUFFICIENT, FollowUpStatus.fromString("SEVERE_INSUFFICIENT"));
        assertEquals(FollowUpStatus.NOT_ASSESSED, FollowUpStatus.fromString("NOT_ASSESSED"));
    }

    @Test
    @DisplayName("fromString 对 null/空/无效值返回 NOT_ASSESSED")
    void testFromStringUnknown()
    {
        assertEquals(FollowUpStatus.NOT_ASSESSED, FollowUpStatus.fromString(null));
        assertEquals(FollowUpStatus.NOT_ASSESSED, FollowUpStatus.fromString(""));
        assertEquals(FollowUpStatus.NOT_ASSESSED, FollowUpStatus.fromString("无效值"));
    }
}
