package com.ruoyi.crm.audit.domain;

import com.ruoyi.crm.common.domain.CrmImmutableEntity;

/**
 * CRM 审计事件对象 crm_audit_event
 * <p>
 * 不可变记录，只追加不修改。
 *
 * @author ruoyi-crm
 */
public class CrmAuditEvent extends CrmImmutableEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;
    /** 事件类型 */
    private String eventType;
    /** 实体类型 */
    private String entityType;
    /** 实体 ID */
    private String entityId;
    /** 操作人 ID */
    private Long operatorId;
    /** 操作人名称 */
    private String operatorName;
    /** 操作（CREATE/UPDATE/DELETE/VIEW） */
    private String action;
    /** 变更前数据（JSON） */
    private String beforeData;
    /** 变更后数据（JSON） */
    private String afterData;
    /** IP 地址 */
    private String ipAddress;
    /** User-Agent */
    private String userAgent;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getEntityType()
    {
        return entityType;
    }

    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public void setEntityId(String entityId)
    {
        this.entityId = entityId;
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

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getBeforeData()
    {
        return beforeData;
    }

    public void setBeforeData(String beforeData)
    {
        this.beforeData = beforeData;
    }

    public String getAfterData()
    {
        return afterData;
    }

    public void setAfterData(String afterData)
    {
        this.afterData = afterData;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }
}
