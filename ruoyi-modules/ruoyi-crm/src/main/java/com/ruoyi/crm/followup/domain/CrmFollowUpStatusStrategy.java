package com.ruoyi.crm.followup.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

import java.util.Date;

/**
 * CRM 跟进状态策略实体 crm_follow_up_status_strategy
 * <p>
 * 管理员为每个企业配置一条生效中的策略，
 * 设置不足阈值天数和严重不足阈值天数。
 *
 * @author ruoyi-crm
 */
public class CrmFollowUpStatusStrategy extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 策略ID（雪花） */
    private Long strategyId;
    /** 不足阈值天数(>=1) */
    private Integer insufficientThreshold;
    /** 严重不足阈值天数(>不足阈值) */
    private Integer severeThreshold;
    /** 生效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveFrom;
    /** 状态(ACTIVE/INACTIVE) */
    private String status;

    public Long getStrategyId()
    {
        return strategyId;
    }

    public void setStrategyId(Long strategyId)
    {
        this.strategyId = strategyId;
    }

    public Integer getInsufficientThreshold()
    {
        return insufficientThreshold;
    }

    public void setInsufficientThreshold(Integer insufficientThreshold)
    {
        this.insufficientThreshold = insufficientThreshold;
    }

    public Integer getSevereThreshold()
    {
        return severeThreshold;
    }

    public void setSevereThreshold(Integer severeThreshold)
    {
        this.severeThreshold = severeThreshold;
    }

    public Date getEffectiveFrom()
    {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom)
    {
        this.effectiveFrom = effectiveFrom;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
