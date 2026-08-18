package com.ruoyi.crm.followup.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

import java.util.Date;

/**
 * CRM 附件实体 crm_attachment
 * <p>
 * 文件上传后进入待扫描状态，病毒扫描通过后变为可用。
 * 电话/微信跟进必须关联至少一条 AVAILABLE 图片附件。
 *
 * @author ruoyi-crm
 */
public class CrmAttachment extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 附件ID（雪花） */
    private Long attachmentId;
    /** 所属业务类型(FOLLOW_UP/CUSTOMER) */
    private String ownerType;
    /** 所属业务对象ID */
    private Long ownerId;
    /** 原始文件名 */
    private String fileName;
    /** MIME类型 */
    private String contentType;
    /** 文件字节数 */
    private Long sizeBytes;
    /** 私有对象存储路径 */
    private String storageKey;
    /** 文件完整性校验值(SHA-256) */
    private String checksum;
    /** 上传人用户ID */
    private Long uploadedBy;
    /** 上传人姓名快照 */
    private String uploadedByName;
    /** 状态(PENDING_SCAN/AVAILABLE/QUARANTINED/DELETED) */
    private String status;
    /** 扫描开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scanStartedAt;
    /** 扫描完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scanCompletedAt;
    /** 脱敏失败码 */
    private String scanErrorCode;

    public Long getAttachmentId()
    {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId)
    {
        this.attachmentId = attachmentId;
    }

    public String getOwnerType()
    {
        return ownerType;
    }

    public void setOwnerType(String ownerType)
    {
        this.ownerType = ownerType;
    }

    public Long getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(Long ownerId)
    {
        this.ownerId = ownerId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public Long getSizeBytes()
    {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes)
    {
        this.sizeBytes = sizeBytes;
    }

    public String getStorageKey()
    {
        return storageKey;
    }

    public void setStorageKey(String storageKey)
    {
        this.storageKey = storageKey;
    }

    public String getChecksum()
    {
        return checksum;
    }

    public void setChecksum(String checksum)
    {
        this.checksum = checksum;
    }

    public Long getUploadedBy()
    {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy)
    {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadedByName()
    {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName)
    {
        this.uploadedByName = uploadedByName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getScanStartedAt()
    {
        return scanStartedAt;
    }

    public void setScanStartedAt(Date scanStartedAt)
    {
        this.scanStartedAt = scanStartedAt;
    }

    public Date getScanCompletedAt()
    {
        return scanCompletedAt;
    }

    public void setScanCompletedAt(Date scanCompletedAt)
    {
        this.scanCompletedAt = scanCompletedAt;
    }

    public String getScanErrorCode()
    {
        return scanErrorCode;
    }

    public void setScanErrorCode(String scanErrorCode)
    {
        this.scanErrorCode = scanErrorCode;
    }
}
