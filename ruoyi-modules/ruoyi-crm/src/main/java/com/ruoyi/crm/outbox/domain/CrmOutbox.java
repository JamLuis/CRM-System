package com.ruoyi.crm.outbox.domain;

import com.ruoyi.crm.common.domain.CrmBaseEntity;

/**
 * Outbox 消息对象 crm_outbox
 * <p>
 * 事务性发件箱模式：业务操作和消息写入同一事务，
 * 由 outbox worker 异步投递消息。
 *
 * @author ruoyi-crm
 */
public class CrmOutbox extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;
    /** 聚合类型 */
    private String aggregateType;
    /** 聚合 ID */
    private String aggregateId;
    /** 事件类型 */
    private String eventType;
    /** 事件负载（JSON） */
    private String payload;
    /** 状态（PENDING/SENDING/SENT/FAILED/DEAD） */
    private String status;
    /** 重试次数 */
    private Integer retryCount;
    /** 下次重试时间 */
    private java.util.Date nextRetryTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getAggregateType()
    {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType)
    {
        this.aggregateType = aggregateType;
    }

    public String getAggregateId()
    {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId)
    {
        this.aggregateId = aggregateId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount)
    {
        this.retryCount = retryCount;
    }

    public java.util.Date getNextRetryTime()
    {
        return nextRetryTime;
    }

    public void setNextRetryTime(java.util.Date nextRetryTime)
    {
        this.nextRetryTime = nextRetryTime;
    }
}
