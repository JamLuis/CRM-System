package com.ruoyi.crm.followup.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 提醒投递状态枚举测试
 */
@DisplayName("提醒投递状态枚举测试")
class ReminderDeliveryStatusTest
{
    @Test
    @DisplayName("fromString 正确解析已知值")
    void testFromStringKnown()
    {
        assertEquals(ReminderDeliveryStatus.PENDING, ReminderDeliveryStatus.fromString("PENDING"));
        assertEquals(ReminderDeliveryStatus.RETRYING, ReminderDeliveryStatus.fromString("RETRYING"));
        assertEquals(ReminderDeliveryStatus.SENT, ReminderDeliveryStatus.fromString("SENT"));
        assertEquals(ReminderDeliveryStatus.COMPLETED, ReminderDeliveryStatus.fromString("COMPLETED"));
        assertEquals(ReminderDeliveryStatus.CANCELLED, ReminderDeliveryStatus.fromString("CANCELLED"));
        assertEquals(ReminderDeliveryStatus.FAILED, ReminderDeliveryStatus.fromString("FAILED"));
    }

    @Test
    @DisplayName("isActive 对 PENDING 和 RETRYING 返回 true")
    void testIsActive()
    {
        assertTrue(ReminderDeliveryStatus.PENDING.isActive());
        assertTrue(ReminderDeliveryStatus.RETRYING.isActive());
        assertFalse(ReminderDeliveryStatus.SENT.isActive());
        assertFalse(ReminderDeliveryStatus.COMPLETED.isActive());
        assertFalse(ReminderDeliveryStatus.CANCELLED.isActive());
        assertFalse(ReminderDeliveryStatus.FAILED.isActive());
    }

    @Test
    @DisplayName("fromString 对未知值抛出异常")
    void testFromStringUnknown()
    {
        assertThrows(IllegalArgumentException.class, () -> ReminderDeliveryStatus.fromString("UNKNOWN"));
        assertThrows(IllegalArgumentException.class, () -> ReminderDeliveryStatus.fromString(null));
    }
}
