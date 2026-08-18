package com.ruoyi.crm.outbox.worker;

import com.ruoyi.crm.outbox.domain.CrmOutbox;

/**
 * Outbox 消息投递器接口
 * <p>
 * 不同的事件类型可以有不同的投递实现（钉钉工作通知、Webhook 等）。
 */
public interface OutboxDispatcher
{
    /**
     * 投递 outbox 消息
     *
     * @param outbox 待投递消息
     * @return true=投递成功, false=投递失败需重试
     */
    boolean dispatch(CrmOutbox outbox);

    /**
     * 此投递器是否支持给定的事件类型
     */
    boolean supports(String eventType);
}
