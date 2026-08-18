package com.ruoyi.crm.outbox.service;

import com.ruoyi.crm.outbox.domain.CrmOutbox;

import java.util.List;

/**
 * Outbox 消息服务接口
 */
public interface OutboxService
{
    /**
     * 创建 outbox 消息（在调用方事务内执行）
     *
     * @param outbox outbox 消息（id 由服务层赋值）
     */
    void create(CrmOutbox outbox);

    /**
     * 查询待发送消息
     */
    List<CrmOutbox> findPending(String tenantId, int limit);

    /**
     * 查询需重试的失败消息
     */
    List<CrmOutbox> findFailedForRetry(String tenantId, int maxRetries, int limit);

    /**
     * 更新消息状态
     */
    boolean updateStatus(Long id, String tenantId, String status, int retryCount, java.util.Date nextRetryTime, int version);

    /**
     * 查询死信
     */
    List<CrmOutbox> findDead(String tenantId);

    /**
     * 将死信重置为待发送
     */
    boolean resetDeadToPending(Long id, String tenantId, int version);
}
