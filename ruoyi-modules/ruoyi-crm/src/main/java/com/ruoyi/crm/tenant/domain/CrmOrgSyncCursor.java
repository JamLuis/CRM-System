package com.ruoyi.crm.tenant.domain;

import com.ruoyi.crm.common.domain.CrmBaseEntity;

/**
 * 组织架构同步游标对象 crm_org_sync_cursor
 *
 * @author ruoyi-crm
 */
public class CrmOrgSyncCursor extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;
    /** 数据源（DINGTALK） */
    private String source;
    /** 同步游标 */
    private String cursor;
    /** 最后同步时间 */
    private java.util.Date lastSyncTime;
    /** 最近一次同步状态（SUCCESS/FAILED） */
    private String status;
    /** 最近一次失败摘要 */
    private String errorSummary;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getCursor()
    {
        return cursor;
    }

    public void setCursor(String cursor)
    {
        this.cursor = cursor;
    }

    public java.util.Date getLastSyncTime()
    {
        return lastSyncTime;
    }

    public void setLastSyncTime(java.util.Date lastSyncTime)
    {
        this.lastSyncTime = lastSyncTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorSummary()
    {
        return errorSummary;
    }

    public void setErrorSummary(String errorSummary)
    {
        this.errorSummary = errorSummary;
    }
}
