package com.ruoyi.crm.followup.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

import java.util.Date;

/**
 * CRM 提醒计划实体 crm_reminder_plan
 * <p>
 * 每客户至多一个活动计划；改期取消旧投递并创建新版本。
 *
 * @author ruoyi-crm
 */
public class CrmReminderPlan extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计划ID（雪花） */
    private Long planId;
    /** 客户ID */
    private Long customerId;
    /** 来源跟进记录ID */
    private Long sourceFollowUpId;
    /** 同一跟进计划的幂等标识 */
    private String planKey;
    /** 计划下一次跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date plannedFollowUpAt;
    /** 提醒发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scheduledAt;
    /** 状态(ACTIVE/CANCELLED/SUPERSEDED) */
    private String status;

    public Long getPlanId()
    {
        return planId;
    }

    public void setPlanId(Long planId)
    {
        this.planId = planId;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Long getSourceFollowUpId()
    {
        return sourceFollowUpId;
    }

    public void setSourceFollowUpId(Long sourceFollowUpId)
    {
        this.sourceFollowUpId = sourceFollowUpId;
    }

    public String getPlanKey()
    {
        return planKey;
    }

    public void setPlanKey(String planKey)
    {
        this.planKey = planKey;
    }

    public Date getPlannedFollowUpAt()
    {
        return plannedFollowUpAt;
    }

    public void setPlannedFollowUpAt(Date plannedFollowUpAt)
    {
        this.plannedFollowUpAt = plannedFollowUpAt;
    }

    public Date getScheduledAt()
    {
        return scheduledAt;
    }

    public void setScheduledAt(Date scheduledAt)
    {
        this.scheduledAt = scheduledAt;
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
