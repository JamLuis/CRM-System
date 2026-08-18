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
import com.ruoyi.crm.followup.service.AttachmentService;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

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
    private IdGenerator idGenerator;

    @Autowired
    private AuditEventService auditEventService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmAttachment createUpload(CrmAttachment attachment)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();

        // 校验 ownerType
        AttachmentOwnerType ownerType = AttachmentOwnerType.fromString(attachment.getOwnerType());

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

        attachmentMapper.insert(attachment);

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

        if (!AttachmentStatus.PENDING_SCAN.name().equals(existing.getStatus()))
        {
            throw new IllegalStateException("附件状态不允许确认上传：" + existing.getStatus());
        }

        // 模拟病毒扫描：直接标记为可用
        // 实际生产中应异步调用杀毒服务
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

        if (!AttachmentStatus.AVAILABLE.name().equals(attachment.getStatus()))
        {
            throw new IllegalStateException("附件不可用，当前状态：" + attachment.getStatus());
        }

        // 实际生产中应在此生成预签名下载URL
        // 目前直接返回 storageKey
        return attachment;
    }

    @Override
    public List<CrmAttachment> listByOwner(String ownerType, Long ownerId)
    {
        String tenantId = TenantContext.getTenantId();
        return attachmentMapper.selectByOwner(tenantId, ownerType, ownerId);
    }

    @Override
    public List<CrmAttachment> listByOwnerAndStatus(String ownerType, Long ownerId, String status)
    {
        String tenantId = TenantContext.getTenantId();
        return attachmentMapper.selectByOwnerAndStatus(tenantId, ownerType, ownerId, status);
    }

    // ==================== Private helpers ====================

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
