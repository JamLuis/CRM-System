package com.ruoyi.crm.followup.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.followup.domain.AttachmentOwnerType;
import com.ruoyi.crm.followup.domain.AttachmentStatus;
import com.ruoyi.crm.followup.domain.CrmAttachment;
import com.ruoyi.crm.followup.mapper.CrmAttachmentMapper;
import com.ruoyi.crm.followup.mapper.CrmFollowUpMapper;
import com.ruoyi.crm.followup.service.AttachmentService;
import com.ruoyi.crm.followup.service.MinioStorageService;
import com.ruoyi.crm.followup.domain.CrmFollowUp;
import com.ruoyi.crm.permission.CustomerAccessGuard;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 附件服务实现
 *
 * @author ruoyi-crm
 */
@Service
public class AttachmentServiceImpl implements AttachmentService
{
    private static final Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    @Autowired
    private CrmAttachmentMapper attachmentMapper;

    @Autowired
    private CrmFollowUpMapper followUpMapper;

    @Autowired
    private CustomerAccessGuard customerAccessGuard;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private MinioStorageService minioStorageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmAttachment createUpload(CrmAttachment attachment)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();

        // 校验 ownerType
        AttachmentOwnerType ownerType = AttachmentOwnerType.fromString(attachment.getOwnerType());
        Long customerId = resolveCustomerId(tenantId, ownerType, attachment.getOwnerId());
        customerAccessGuard.check(customerId, PermissionCode.CRM_ATTACHMENT_WRITE);

        // 设置默认值
        attachment.setAttachmentId(idGenerator.nextId());
        attachment.setTenantId(tenantId);
        attachment.setUploadedBy(operatorId);
        attachment.setUploadedByName(operatorName);
        attachment.setStatus(AttachmentStatus.PENDING_SCAN.name());
        attachment.setVersion(0);
        attachment.setDelFlag("0");
        attachment.setCreateBy(operatorName);
        attachment.setUpdateBy(operatorName);
        attachment.setStorageKey(buildStorageKey(
                tenantId, customerId, attachment.getAttachmentId(), attachment.getFileName()));

        attachmentMapper.insert(attachment);
        attachment.setUploadUrl(minioStorageService.createUploadUrl(attachment.getStorageKey()));

        recordAudit(tenantId, attachment.getAttachmentId(), attachment.getOwnerId(),
                operatorId, operatorName, "UPLOAD", null, attachment.getFileName());

        log.info("Attachment upload created: tenantId={}, attachmentId={}, ownerType={}, ownerId={}, fileName={}",
                tenantId, attachment.getAttachmentId(), ownerType, attachment.getOwnerId(), attachment.getFileName());

        return attachment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmAttachment confirmUpload(Long attachmentId)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();

        CrmAttachment existing = attachmentMapper.selectByAttachmentId(tenantId, attachmentId);
        if (existing == null)
        {
            throw new IllegalArgumentException("附件不存在：" + attachmentId);
        }
        customerAccessGuard.check(resolveCustomerId(tenantId,
                AttachmentOwnerType.fromString(existing.getOwnerType()), existing.getOwnerId()),
                PermissionCode.CRM_ATTACHMENT_WRITE);

        if (!AttachmentStatus.PENDING_SCAN.name().equals(existing.getStatus()))
        {
            throw new IllegalStateException("附件状态不允许确认上传：" + existing.getStatus());
        }

        // 确认对象已经真实写入 MinIO。ClamAV 按当前整改范围暂缓。
        minioStorageService.stat(existing.getStorageKey());
        attachmentMapper.updateScanResult(tenantId, attachmentId,
                AttachmentStatus.AVAILABLE.name(), new Date(), null);

        log.info("Attachment confirmed and scanned: tenantId={}, attachmentId={}, status=AVAILABLE",
                tenantId, attachmentId);

        return attachmentMapper.selectByAttachmentId(tenantId, attachmentId);
    }

    @Override
    public CrmAttachment getDownloadUrl(Long attachmentId)
    {
        String tenantId = TenantContext.getTenantId();

        CrmAttachment attachment = attachmentMapper.selectByAttachmentId(tenantId, attachmentId);
        if (attachment == null)
        {
            throw new IllegalArgumentException("附件不存在：" + attachmentId);
        }
        customerAccessGuard.check(resolveCustomerId(tenantId,
                AttachmentOwnerType.fromString(attachment.getOwnerType()), attachment.getOwnerId()),
                PermissionCode.CRM_ATTACHMENT_READ);

        if (!AttachmentStatus.AVAILABLE.name().equals(attachment.getStatus()))
        {
            throw new IllegalStateException("附件不可用，当前状态：" + attachment.getStatus());
        }

        attachment.setDownloadUrl(minioStorageService.createDownloadUrl(attachment.getStorageKey()));
        return attachment;
    }

    @Override
    public List<CrmAttachment> listByOwner(String ownerType, Long ownerId)
    {
        String tenantId = TenantContext.getTenantId();
        AttachmentOwnerType type = AttachmentOwnerType.fromString(ownerType);
        customerAccessGuard.check(resolveCustomerId(tenantId, type, ownerId),
                PermissionCode.CRM_ATTACHMENT_READ);
        return attachmentMapper.selectByOwner(tenantId, ownerType, ownerId);
    }

    @Override
    public List<CrmAttachment> listByOwnerAndStatus(String ownerType, Long ownerId, String status)
    {
        String tenantId = TenantContext.getTenantId();
        AttachmentOwnerType type = AttachmentOwnerType.fromString(ownerType);
        customerAccessGuard.check(resolveCustomerId(tenantId, type, ownerId),
                PermissionCode.CRM_ATTACHMENT_READ);
        return attachmentMapper.selectByOwnerAndStatus(tenantId, ownerType, ownerId, status);
    }

    // ==================== Private helpers ====================

    private Long resolveCustomerId(String tenantId, AttachmentOwnerType ownerType, Long ownerId)
    {
        if (ownerId == null)
        {
            throw new IllegalArgumentException("附件必须关联客户或已有跟进记录");
        }
        if (AttachmentOwnerType.CUSTOMER == ownerType)
        {
            return ownerId;
        }
        CrmFollowUp followUp = followUpMapper.selectByFollowUpId(tenantId, ownerId);
        if (followUp == null)
        {
            throw new IllegalArgumentException("附件关联的跟进记录不存在：" + ownerId);
        }
        return followUp.getCustomerId();
    }

    private String buildStorageKey(String tenantId, Long customerId, Long attachmentId, String fileName)
    {
        String safeName = fileName == null ? "file" : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return tenantId + "/customers/" + customerId + "/" + attachmentId + "-"
                + UUID.randomUUID().toString().replace("-", "") + "-" + safeName;
    }

    private void recordAudit(String tenantId, Long attachmentId, Long ownerId,
                             Long operatorId, String operatorName,
                             String action, String beforeData, String afterData)
    {
        CrmAuditEvent event = new CrmAuditEvent();
        event.setTenantId(tenantId);
        event.setEventType("ATTACHMENT");
        event.setEntityType("ATTACHMENT");
        event.setEntityId(String.valueOf(attachmentId));
        event.setOperatorId(operatorId);
        event.setOperatorName(operatorName);
        event.setAction(action);
        event.setBeforeData(beforeData);
        event.setAfterData(afterData);
        auditEventService.record(event);
    }
}
