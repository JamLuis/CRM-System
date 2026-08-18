package com.ruoyi.crm.followup.adapter;

import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 钉钉提醒适配器测试
 */
@DisplayName("钉钉提醒适配器测试")
class DingTalkReminderAdapterTest
{
    @Test
    @DisplayName("发送提醒 - 模拟发送返回 true")
    void testSendReturnsTrue()
    {
        DingTalkReminderAdapter adapter = new DingTalkReminderAdapter();

        CrmReminderDelivery delivery = new CrmReminderDelivery();
        delivery.setDeliveryId(7001L);
        delivery.setRecipientUserId(1L);
        delivery.setCustomerId(1001L);

        boolean result = adapter.send(delivery);

        assertTrue(result);
    }

    @Test
    @DisplayName("计算重试间隔 - 第一次重试间隔为5分钟")
    void testCalculateRetryIntervalFirstRetry()
    {
        DingTalkReminderAdapter adapter = new DingTalkReminderAdapter();

        long interval = adapter.calculateRetryInterval(0);

        assertEquals(5 * 60 * 1000L, interval);
    }

    @Test
    @DisplayName("计算重试间隔 - 第二次重试间隔为30分钟")
    void testCalculateRetryIntervalSecondRetry()
    {
        DingTalkReminderAdapter adapter = new DingTalkReminderAdapter();

        long interval = adapter.calculateRetryInterval(1);

        assertEquals(30 * 60 * 1000L, interval);
    }

    @Test
    @DisplayName("计算重试间隔 - 第三次重试间隔为2小时")
    void testCalculateRetryIntervalThirdRetry()
    {
        DingTalkReminderAdapter adapter = new DingTalkReminderAdapter();

        long interval = adapter.calculateRetryInterval(2);

        assertEquals(2 * 60 * 60 * 1000L, interval);
    }

    @Test
    @DisplayName("计算重试间隔 - 超过最大重试次数返回 -1")
    void testCalculateRetryIntervalExceedsMax()
    {
        DingTalkReminderAdapter adapter = new DingTalkReminderAdapter();

        long interval = adapter.calculateRetryInterval(3);

        assertEquals(-1, interval);
    }
}
