package com.ruoyi.crm.followup.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

import java.util.Date;

/**
 * CRM 提醒投递任务实体 crm_reminder_delivery
 * <p>
 * 同一计划/接收人/版本最多一条有效消息；重复消费无副作用。
 * 重试最多3次，最终失败后告警。
 *
 * @author ruoyi-crm
 */
public class CrmReminderDelivery extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 投递ID（雪花） */
    private Long deliveryId;
    /** 提醒计划ID */
    private Long planId;
    /** 客户ID */
    private Long customerId;
    /** 同一跟进计划的幂等标识 */
    private String planKey;
    /** 计划下一次跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date plannedFollowUpAt;
    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scheduledAt;
    /** 接收人用户ID */
    private Long recipientUserId;
    /** 接收人姓名快照 */
    private String recipientName;
    /** 状态(PENDING/RETRYING/SENT/COMPLETED/CANCELLED/FAILED) */
    private String status;
    /** 已执行重试次数，最大3 */
    private Integer retryCount;
    /** 最近一次发送尝试时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastAttemptAt;
    /** 脱敏后的最近失败码 */
    private String lastErrorCode;
    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedAt;
    /** 客户名称（联查展示字段，非持久化） */
    private String customerName;

    public Long getDeliveryId()
    {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId)
    {
        this.deliveryId = deliveryId;
    }

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

    public Long getRecipientUserId()
    {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId)
    {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientName()
    {
        return recipientName;
    }

    public void setRecipientName(String recipientName)
    {
        this.recipientName = recipientName;
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

    public Date getLastAttemptAt()
    {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Date lastAttemptAt)
    {
        this.lastAttemptAt = lastAttemptAt;
    }

    public String getLastErrorCode()
    {
        return lastErrorCode;
    }

    public void setLastErrorCode(String lastErrorCode)
    {
        this.lastErrorCode = lastErrorCode;
    }

    public Date getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt)
    {
        this.completedAt = completedAt;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }
}
