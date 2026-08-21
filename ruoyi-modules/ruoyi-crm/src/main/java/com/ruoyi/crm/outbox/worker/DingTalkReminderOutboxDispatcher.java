package com.ruoyi.crm.outbox.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.crm.followup.adapter.DingTalkReminderAdapter;
import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import com.ruoyi.crm.followup.service.ReminderService;
import com.ruoyi.crm.outbox.domain.CrmOutbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class DingTalkReminderOutboxDispatcher implements OutboxDispatcher
{
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DingTalkReminderAdapter reminderAdapter;

    @Autowired
    private ReminderService reminderService;

    @Override
    public boolean dispatch(CrmOutbox outbox)
    {
        try
        {
            CrmReminderDelivery delivery =
                    objectMapper.readValue(outbox.getPayload(), CrmReminderDelivery.class);
            delivery.setTenantId(outbox.getTenantId());
            boolean sent = reminderAdapter.send(delivery);
            if (sent)
            {
                reminderService.markSent(delivery.getDeliveryId(), "outbox-worker");
            }
            return sent;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("钉钉提醒投递失败", e);
        }
    }

    @Override
    public boolean supports(String eventType)
    {
        return "DINGTALK_REMINDER".equals(eventType);
    }
}
