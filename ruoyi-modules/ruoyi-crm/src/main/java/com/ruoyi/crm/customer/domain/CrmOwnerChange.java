package com.ruoyi.crm.customer.domain;

import com.ruoyi.crm.common.domain.CrmImmutableEntity;

/**
 * CRM 负责人变更记录实体 crm_owner_change
 * <p>
 * 不可变表，记录每次主负责人分配、移交和协同人增减。
 *
 * @author ruoyi-crm
 */
public class CrmOwnerChange extends CrmImmutableEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键（雪花） */
    private Long id;
    /** 客户ID */
    private Long customerId;
    /** 变更类型(ASSIGN/TRANSFER/COLLABORATOR_ADD/COLLABORATOR_REMOVE) */
    private String changeType;
    /** 原主负责人ID */
    private Long previousPrimaryOwnerId;
    /** 原主负责人姓名快照 */
    private String previousPrimaryOwnerName;
    /** 新主负责人ID */
    private Long targetPrimaryOwnerId;
    /** 新主负责人姓名快照 */
    private String targetPrimaryOwnerName;
    /** 新增协同人ID列表 */
    private String addedCollaboratorIds;
    /** 移除协同人ID列表 */
    private String removedCollaboratorIds;
    /** 移交时是否保留原负责人为协同人 */
    private Boolean keepPreviousAsCollaborator;
    /** 变更原因 */
    private String reason;
    /** 操作人ID */
    private Long operatorId;
    /** 操作人姓名 */
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

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    public Long getPreviousPrimaryOwnerId()
    {
        return previousPrimaryOwnerId;
    }

    public void setPreviousPrimaryOwnerId(Long previousPrimaryOwnerId)
    {
        this.previousPrimaryOwnerId = previousPrimaryOwnerId;
    }

    public String getPreviousPrimaryOwnerName()
    {
        return previousPrimaryOwnerName;
    }

    public void setPreviousPrimaryOwnerName(String previousPrimaryOwnerName)
    {
        this.previousPrimaryOwnerName = previousPrimaryOwnerName;
    }

    public Long getTargetPrimaryOwnerId()
    {
        return targetPrimaryOwnerId;
    }

    public void setTargetPrimaryOwnerId(Long targetPrimaryOwnerId)
    {
        this.targetPrimaryOwnerId = targetPrimaryOwnerId;
    }

    public String getTargetPrimaryOwnerName()
    {
        return targetPrimaryOwnerName;
    }

    public void setTargetPrimaryOwnerName(String targetPrimaryOwnerName)
    {
        this.targetPrimaryOwnerName = targetPrimaryOwnerName;
    }

    public String getAddedCollaboratorIds()
    {
        return addedCollaboratorIds;
    }

    public void setAddedCollaboratorIds(String addedCollaboratorIds)
    {
        this.addedCollaboratorIds = addedCollaboratorIds;
    }

    public String getRemovedCollaboratorIds()
    {
        return removedCollaboratorIds;
    }

    public void setRemovedCollaboratorIds(String removedCollaboratorIds)
    {
        this.removedCollaboratorIds = removedCollaboratorIds;
    }

    public Boolean getKeepPreviousAsCollaborator()
    {
        return keepPreviousAsCollaborator;
    }

    public void setKeepPreviousAsCollaborator(Boolean keepPreviousAsCollaborator)
    {
        this.keepPreviousAsCollaborator = keepPreviousAsCollaborator;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
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
