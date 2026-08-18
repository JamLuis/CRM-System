package com.ruoyi.crm.outbox.mapper;

import com.ruoyi.crm.outbox.domain.CrmOutbox;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Outbox Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmOutboxMapper
{
    /**
     * 新增 outbox 消息
     */
    int insert(CrmOutbox outbox);

    /**
     * 查询待发送消息
     */
    List<CrmOutbox> selectPending(@Param("tenantId") String tenantId, @Param("limit") int limit);

    /**
     * 查询失败且需要重试的消息
     */
    List<CrmOutbox> selectFailedForRetry(@Param("tenantId") String tenantId,
                                         @Param("maxRetries") int maxRetries,
                                         @Param("limit") int limit);

    /**
     * 更新消息状态（乐观锁）
     */
    int updateStatus(@Param("id") Long id,
                     @Param("tenantId") String tenantId,
                     @Param("status") String status,
                     @Param("retryCount") int retryCount,
                     @Param("nextRetryTime") Date nextRetryTime,
                     @Param("version") int version);

    /**
     * 查询死信（超过最大重试次数）
     */
    List<CrmOutbox> selectDead(@Param("tenantId") String tenantId);

    /**
     * 重置死信为待重试（人工重放，乐观锁）
     */
    int resetDeadToPending(@Param("id") Long id,
                           @Param("tenantId") String tenantId,
                           @Param("version") int version);
}
