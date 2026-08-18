package com.ruoyi.crm.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;

/**
 * CRM 不可变记录基类
 * <p>
 * 用于审计事件、时间线等只追加（append-only）表。
 * 不含乐观锁和逻辑删除字段。
 *
 * @author ruoyi-crm
 */
public class CrmImmutableEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 租户 ID */
    @JsonIgnore
    private String tenantId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
