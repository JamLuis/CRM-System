package com.ruoyi.crm.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * CRM 实体基类
 * <p>
 * 在 RuoYi {@link BaseEntity} 基础上增加：
 * <ul>
 *   <li>{@code tenantId} — 租户上下文，所有 CRM 查询必须携带</li>
 *   <li>{@code version} — 乐观锁版本号，更新时自增</li>
 *   <li>{@code delFlag} — 逻辑删除标志（0=正常 2=删除）</li>
 * </ul>
 *
 * @author ruoyi-crm
 */
public class CrmBaseEntity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 租户 ID */
    @JsonIgnore
    private String tenantId;

    /** 乐观锁版本号 */
    private Integer version;

    /** 删除标志（0=正常 2=删除） */
    private String delFlag;

    /**
     * 获取租户 ID
     */
    public String getTenantId()
    {
        return tenantId;
    }

    /**
     * 设置租户 ID
     */
    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }

    /**
     * 获取版本号
     */
    public Integer getVersion()
    {
        return version;
    }

    /**
     * 设置版本号
     */
    public void setVersion(Integer version)
    {
        this.version = version;
    }

    /**
     * 获取删除标志
     */
    public String getDelFlag()
    {
        return delFlag;
    }

    /**
     * 设置删除标志
     */
    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
