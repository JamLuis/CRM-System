package com.ruoyi.crm.audit.domain;

import com.ruoyi.crm.common.domain.CrmImmutableEntity;

/**
 * 客户时间线对象 crm_customer_timeline
 * <p>
 * 不可变记录，用于客户 360 视图的动态流。
 *
 * @author ruoyi-crm
 */
public class CrmCustomerTimeline extends CrmImmutableEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;
    /** 客户 ID */
    private Long customerId;
    /** 事件类型 */
    private String eventType;
    /** 事件数据（JSON） */
    private String eventData;
    /** 操作人 ID */
    private Long operatorId;
    /** 操作人名称 */
    private String operatorName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getEventData()
    {
        return eventData;
    }

    public void setEventData(String eventData)
    {
        this.eventData = eventData;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }
}
