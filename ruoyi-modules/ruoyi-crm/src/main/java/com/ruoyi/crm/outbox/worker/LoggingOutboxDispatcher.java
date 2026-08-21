package com.ruoyi.crm.outbox.worker;

import com.ruoyi.crm.outbox.domain.CrmOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

/**
 * 日志投递器 — 默认实现，将 outbox 消息输出到日志
 * <p>
 * 用于开发/测试环境，或作为未匹配到专用投递器时的兜底。
 */
@Component
@Order(1000)
public class LoggingOutboxDispatcher implements OutboxDispatcher
{
    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxDispatcher.class);

    @Override
    public boolean dispatch(CrmOutbox outbox)
    {
        log.info("[OutboxDispatch] id={}, tenant={}, eventType={}, aggregateType={}, aggregateId={}, payload={}",
                outbox.getId(), outbox.getTenantId(), outbox.getEventType(),
                outbox.getAggregateType(), outbox.getAggregateId(), outbox.getPayload());
        return true;
    }

    @Override
    public boolean supports(String eventType)
    {
        return !"DINGTALK_REMINDER".equals(eventType);
    }
}
