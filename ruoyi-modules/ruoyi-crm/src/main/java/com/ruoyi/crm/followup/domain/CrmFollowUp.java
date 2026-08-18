package com.ruoyi.crm.followup.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

import java.util.Date;

/**
 * CRM 跟进记录实体 crm_follow_up
 * <p>
 * 跟进记录提交后不可修改正文和时间；发现错误时创建一条"更正跟进"。
 * 更正记录引用原记录并说明更正原因。
 *
 * @author ruoyi-crm
 */
public class CrmFollowUp extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 跟进记录ID（雪花） */
    private Long followUpId;
    /** 客户ID */
    private Long customerId;
    /** 跟进方式(PHONE/WECHAT/IN_PERSON) */
    private String method;
    /** 跟进发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date followUpAt;
    /** 跟进内容 */
    private String content;
    /** 是否跟进新签项目 */
    private Boolean hasNewSigningProject;
    /** 本次跟进结果摘要 */
    private String outcome;
    /** 下一步行动说明 */
    private String nextAction;
    /** 下一次跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextFollowUpAt;
    /** 无下一次跟进计划原因 */
    private String noNextFollowUpReason;
    /** 更正记录引用的原跟进ID */
    private Long correctionOfFollowUpId;
    /** 更正原因 */
    private String correctionReason;
    /** 是否已被更正 */
    private Boolean isCorrected;
    /** 是否已作废 */
    private Boolean isVoided;
    /** 作废原因 */
    private String voidedReason;
    /** 创建人用户ID */
    private Long createdBy;
    /** 创建人姓名快照 */
    private String createdByName;
    /** 提交完成时间，之后正文不可直接更新 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date immutableAt;

    public Long getFollowUpId()
    {
        return followUpId;
    }

    public void setFollowUpId(Long followUpId)
    {
        this.followUpId = followUpId;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public Date getFollowUpAt()
    {
        return followUpAt;
    }

    public void setFollowUpAt(Date followUpAt)
    {
        this.followUpAt = followUpAt;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Boolean getHasNewSigningProject()
    {
        return hasNewSigningProject;
    }

    public void setHasNewSigningProject(Boolean hasNewSigningProject)
    {
        this.hasNewSigningProject = hasNewSigningProject;
    }

    public String getOutcome()
    {
        return outcome;
    }

    public void setOutcome(String outcome)
    {
        this.outcome = outcome;
    }

    public String getNextAction()
    {
        return nextAction;
    }

    public void setNextAction(String nextAction)
    {
        this.nextAction = nextAction;
    }

    public Date getNextFollowUpAt()
    {
        return nextFollowUpAt;
    }

    public void setNextFollowUpAt(Date nextFollowUpAt)
    {
        this.nextFollowUpAt = nextFollowUpAt;
    }

    public String getNoNextFollowUpReason()
    {
        return noNextFollowUpReason;
    }

    public void setNoNextFollowUpReason(String noNextFollowUpReason)
    {
        this.noNextFollowUpReason = noNextFollowUpReason;
    }

    public Long getCorrectionOfFollowUpId()
    {
        return correctionOfFollowUpId;
    }

    public void setCorrectionOfFollowUpId(Long correctionOfFollowUpId)
    {
        this.correctionOfFollowUpId = correctionOfFollowUpId;
    }

    public String getCorrectionReason()
    {
        return correctionReason;
    }

    public void setCorrectionReason(String correctionReason)
    {
        this.correctionReason = correctionReason;
    }

    public Boolean getIsCorrected()
    {
        return isCorrected;
    }

    public void setIsCorrected(Boolean isCorrected)
    {
        this.isCorrected = isCorrected;
    }

    public Boolean getIsVoided()
    {
        return isVoided;
    }

    public void setIsVoided(Boolean isVoided)
    {
        this.isVoided = isVoided;
    }

    public String getVoidedReason()
    {
        return voidedReason;
    }

    public void setVoidedReason(String voidedReason)
    {
        this.voidedReason = voidedReason;
    }

    public Long getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy)
    {
        this.createdBy = createdBy;
    }

    public String getCreatedByName()
    {
        return createdByName;
    }

    public void setCreatedByName(String createdByName)
    {
        this.createdByName = createdByName;
    }

    public Date getImmutableAt()
    {
        return immutableAt;
    }

    public void setImmutableAt(Date immutableAt)
    {
        this.immutableAt = immutableAt;
    }
}
