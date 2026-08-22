package com.ruoyi.crm.followup.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.followup.domain.*;
import com.ruoyi.crm.followup.mapper.CrmAttachmentMapper;
import com.ruoyi.crm.followup.mapper.CrmFollowUpContactMapper;
import com.ruoyi.crm.followup.mapper.CrmFollowUpMapper;
import com.ruoyi.crm.followup.service.FollowUpService;
import com.ruoyi.crm.followup.service.FollowUpStatusService;
import com.ruoyi.crm.followup.service.ReminderService;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.crm.permission.CustomerAccessGuard;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 跟进记录服务实现
 *
 * @author ruoyi-crm
 */
@Service
public class FollowUpServiceImpl implements FollowUpService
{
    private static final Logger log = LoggerFactory.getLogger(FollowUpServiceImpl.class);

    /** 跟进时间最大允许前推天数 */
    private static final int MAX_PAST_DAYS = 30;

    @Autowired
    private CrmFollowUpMapper followUpMapper;

    @Autowired
    private CrmFollowUpContactMapper followUpContactMapper;

    @Autowired
    private CrmAttachmentMapper attachmentMapper;

    @Autowired
    private CrmCustomerMapper customerMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private CustomerAccessGuard customerAccessGuard;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private CustomerTimelineService timelineService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private FollowUpStatusService followUpStatusService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmFollowUp create(CrmFollowUp followUp, List<Long> contactIds, List<Long> attachmentIds)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 查询客户
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, followUp.getCustomerId());
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + followUp.getCustomerId());
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_FOLLOWUP_WRITE);
        permissionService.check(ctx);

        // 校验跟进方式
        FollowUpMethod method = FollowUpMethod.fromString(followUp.getMethod());

        // 校验跟进时间范围
        validateFollowUpAt(followUp.getFollowUpAt(), isAdmin);

        // PHONE/WECHAT 必须至少一条 AVAILABLE 图片附件
        if (method == FollowUpMethod.PHONE || method == FollowUpMethod.WECHAT)
        {
            validateImageAttachments(tenantId, followUp.getCustomerId(), attachmentIds);
        }

        // 设置默认值
        Date now = new Date();
        followUp.setFollowUpId(idGenerator.nextId());
        followUp.setTenantId(tenantId);
        if (followUp.getHasNewSigningProject() == null)
        {
            followUp.setHasNewSigningProject(false);
        }
        followUp.setIsCorrected(false);
        followUp.setIsVoided(false);
        followUp.setCreatedBy(operatorId);
        followUp.setCreatedByName(operatorName);
        followUp.setImmutableAt(now);
        followUp.setVersion(0);
        followUp.setDelFlag("0");
        followUp.setCreateBy(operatorName);
        followUp.setUpdateBy(operatorName);

        followUpMapper.insert(followUp);

        // 保存联系人关联
        if (contactIds != null && !contactIds.isEmpty())
        {
            List<CrmFollowUpContact> list = new ArrayList<>();
            for (Long contactId : contactIds)
            {
                CrmFollowUpContact fc = new CrmFollowUpContact();
                fc.setId(idGenerator.nextId());
                fc.setTenantId(tenantId);
                fc.setFollowUpId(followUp.getFollowUpId());
                fc.setContactId(contactId);
                list.add(fc);
            }
            followUpContactMapper.batchInsert(list);
        }

        // 关联附件到跟进记录
        if (attachmentIds != null && !attachmentIds.isEmpty())
        {
            for (Long attachmentId : attachmentIds)
            {
                CrmAttachment attachment = attachmentMapper.selectByAttachmentId(tenantId, attachmentId);
                if (attachment != null)
                {
                    attachment.setOwnerType(AttachmentOwnerType.FOLLOW_UP.name());
                    attachment.setOwnerId(followUp.getFollowUpId());
                    attachmentMapper.updateStatus(tenantId, attachmentId,
                            attachment.getStatus(), operatorName);
                }
            }
        }

        // 创建提醒计划
        if (followUp.getNextFollowUpAt() != null)
        {
            reminderService.createPlan(followUp.getFollowUpId(), followUp.getCustomerId(),
                    followUp.getNextFollowUpAt(), operatorId, operatorName);
        }

        // 更新客户最近有效跟进时间和下一次跟进时间
        int rows = customerMapper.updateFollowUpTimestamps(tenantId, followUp.getCustomerId(),
                followUp.getFollowUpAt(), followUp.getNextFollowUpAt(), operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }

        // 重算客户跟进状态（健康度）
        followUpStatusService.calculate(followUp.getCustomerId());

        recordAudit(tenantId, followUp.getFollowUpId(), followUp.getCustomerId(),
                operatorId, operatorName, "CREATE", null, followUp.getContent());
        recordTimeline(tenantId, followUp.getCustomerId(), operatorId, operatorName,
                "FOLLOWUP_CREATED", followUp.getMethod());

        log.info("Follow-up created: tenantId={}, followUpId={}, customerId={}, method={}, operator={}",
                tenantId, followUp.getFollowUpId(), followUp.getCustomerId(), method, operatorName);

        return followUpMapper.selectByFollowUpId(tenantId, followUp.getFollowUpId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmFollowUp correct(Long originalFollowUpId, CrmFollowUp correction,
                               List<Long> contactIds, List<Long> attachmentIds,
                               String correctionReason)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 查询原跟进记录
        CrmFollowUp original = followUpMapper.selectByFollowUpId(tenantId, originalFollowUpId);
        if (original == null)
        {
            throw new IllegalArgumentException("原跟进记录不存在：" + originalFollowUpId);
        }

        if (original.getIsVoided())
        {
            throw new IllegalStateException("已作废的跟进记录不允许更正");
        }

        // 查询客户用于权限校验
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, original.getCustomerId());
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + original.getCustomerId());
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_FOLLOWUP_WRITE);
        permissionService.check(ctx);

        // 校验跟进方式
        FollowUpMethod method = FollowUpMethod.fromString(correction.getMethod());

        // 校验跟进时间范围
        validateFollowUpAt(correction.getFollowUpAt(), isAdmin);

        // PHONE/WECHAT 必须至少一条 AVAILABLE 图片附件
        if (method == FollowUpMethod.PHONE || method == FollowUpMethod.WECHAT)
        {
            validateImageAttachments(tenantId, correction.getCustomerId(), attachmentIds);
        }

        // 创建更正记录
        Date now = new Date();
        correction.setFollowUpId(idGenerator.nextId());
        correction.setTenantId(tenantId);
        correction.setCustomerId(original.getCustomerId());
        correction.setCorrectionOfFollowUpId(originalFollowUpId);
        correction.setCorrectionReason(correctionReason);
        if (correction.getHasNewSigningProject() == null)
        {
            correction.setHasNewSigningProject(false);
        }
        correction.setIsCorrected(false);
        correction.setIsVoided(false);
        correction.setCreatedBy(operatorId);
        correction.setCreatedByName(operatorName);
        correction.setImmutableAt(now);
        correction.setVersion(0);
        correction.setDelFlag("0");
        correction.setCreateBy(operatorName);
        correction.setUpdateBy(operatorName);

        followUpMapper.insert(correction);

        // 标记原记录已被更正
        followUpMapper.markCorrected(tenantId, originalFollowUpId);

        // 保存联系人关联
        if (contactIds != null && !contactIds.isEmpty())
        {
            List<CrmFollowUpContact> list = new ArrayList<>();
            for (Long contactId : contactIds)
            {
                CrmFollowUpContact fc = new CrmFollowUpContact();
                fc.setId(idGenerator.nextId());
                fc.setTenantId(tenantId);
                fc.setFollowUpId(correction.getFollowUpId());
                fc.setContactId(contactId);
                list.add(fc);
            }
            followUpContactMapper.batchInsert(list);
        }

        // 更新客户最近有效跟进时间和下一次跟进时间，并重算跟进状态
        int rows = customerMapper.updateFollowUpTimestamps(tenantId, correction.getCustomerId(),
                correction.getFollowUpAt(), correction.getNextFollowUpAt(), operatorName, customer.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("客户已被其他操作修改，请刷新后重试");
        }
        followUpStatusService.calculate(correction.getCustomerId());

        recordAudit(tenantId, correction.getFollowUpId(), correction.getCustomerId(),
                operatorId, operatorName, "CORRECT", String.valueOf(originalFollowUpId),
                correctionReason);
        recordTimeline(tenantId, correction.getCustomerId(), operatorId, operatorName,
                "FOLLOWUP_CORRECTED", String.valueOf(originalFollowUpId));

        log.info("Follow-up corrected: tenantId={}, originalFollowUpId={}, newFollowUpId={}, operator={}",
                tenantId, originalFollowUpId, correction.getFollowUpId(), operatorName);

        return followUpMapper.selectByFollowUpId(tenantId, correction.getFollowUpId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmFollowUp void_(Long followUpId, String voidedReason)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        CrmFollowUp existing = followUpMapper.selectByFollowUpId(tenantId, followUpId);
        if (existing == null)
        {
            throw new IllegalArgumentException("跟进记录不存在：" + followUpId);
        }

        if (existing.getIsVoided())
        {
            throw new IllegalStateException("跟进记录已作废");
        }

        // 查询客户用于权限校验
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, existing.getCustomerId());
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + existing.getCustomerId());
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_FOLLOWUP_WRITE);
        permissionService.check(ctx);

        int rows = followUpMapper.markVoided(tenantId, followUpId, voidedReason, operatorName);
        if (rows == 0)
        {
            throw new IllegalStateException("跟进记录已被其他操作修改，请刷新后重试");
        }

        recordAudit(tenantId, followUpId, existing.getCustomerId(),
                operatorId, operatorName, "VOID", null, voidedReason);
        recordTimeline(tenantId, existing.getCustomerId(), operatorId, operatorName,
                "FOLLOWUP_VOIDED", String.valueOf(followUpId));

        log.info("Follow-up voided: tenantId={}, followUpId={}, operator={}",
                tenantId, followUpId, operatorName);

        return followUpMapper.selectByFollowUpId(tenantId, followUpId);
    }

    @Override
    public CrmFollowUp detail(Long followUpId)
    {
        String tenantId = TenantContext.getTenantId();
        CrmFollowUp followUp = followUpMapper.selectByFollowUpId(tenantId, followUpId);
        if (followUp == null)
        {
            throw new IllegalArgumentException("跟进记录不存在：" + followUpId);
        }

        // 权限校验
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, followUp.getCustomerId());
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + followUp.getCustomerId());
        }

        Long operatorId = SecurityUtils.getUserId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_FOLLOWUP_READ);
        permissionService.check(ctx);

        return followUp;
    }

    @Override
    public List<CrmFollowUp> listByCustomer(Long customerId)
    {
        customerAccessGuard.check(customerId, PermissionCode.CRM_FOLLOWUP_READ);
        String tenantId = TenantContext.getTenantId();
        return followUpMapper.selectByCustomer(tenantId, customerId);
    }

    // ==================== Private helpers ====================

    /**
     * 校验跟进时间范围：不能是未来时间，不能早于30天前
     */
    private void validateFollowUpAt(Date followUpAt, boolean isAdmin)
    {
        if (followUpAt == null)
        {
            throw new IllegalArgumentException("跟进时间不能为空");
        }

        Date now = new Date();
        if (followUpAt.after(now))
        {
            throw new IllegalArgumentException("跟进时间不能是未来时间");
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -MAX_PAST_DAYS);
        Date minDate = cal.getTime();

        if (followUpAt.before(minDate))
        {
            if (!isAdmin)
            {
                throw new IllegalArgumentException("跟进时间不能早于" + MAX_PAST_DAYS + "天前，如需补录请联系销售经理或管理员");
            }
            // 管理员允许补录超过30天的跟进，但需要说明原因（由调用方传入 correctionReason）
        }
    }

    /**
     * 校验 PHONE/WECHAT 跟进必须至少一条 AVAILABLE 图片附件
     */
    private void validateImageAttachments(String tenantId, Long customerId, List<Long> attachmentIds)
    {
        if (attachmentIds == null || attachmentIds.isEmpty())
        {
            throw new IllegalArgumentException("电话/微信跟进必须上传至少一张图片附件");
        }

        boolean hasAvailableImage = false;
        for (Long attachmentId : attachmentIds)
        {
            CrmAttachment attachment = attachmentMapper.selectByAttachmentId(tenantId, attachmentId);
            if (attachment != null
                    && AttachmentStatus.AVAILABLE.name().equals(attachment.getStatus())
                    && AttachmentOwnerType.CUSTOMER.name().equals(attachment.getOwnerType())
                    && customerId.equals(attachment.getOwnerId())
                    && attachment.getContentType() != null
                    && attachment.getContentType().startsWith("image/"))
            {
                hasAvailableImage = true;
                break;
            }
        }

        if (!hasAvailableImage)
        {
            throw new IllegalArgumentException("电话/微信跟进必须至少上传一张可用状态的图片附件");
        }
    }

    private PermissionContext buildPermissionContext(CrmCustomer customer, Long operatorId,
                                                    Long operatorDeptId, boolean isAdmin,
                                                    PermissionCode permissionCode)
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(operatorId);
        ctx.setOperatorDeptId(operatorDeptId);
        ctx.setAdmin(isAdmin);
        ctx.setPermissionCode(permissionCode);
        ctx.setPrimaryOwnerId(customer.getPrimaryOwnerId());
        ctx.setCollaboratorIds(customer.getCollaboratorIds());
        ctx.setCreatorDeptId(customer.getCreatorDeptId());
        ctx.setOwnerDeptId(customer.getOwnerDeptId());
        ctx.setOperatingStatus(customer.getOperatingStatus());
        return ctx;
    }

    private void recordAudit(String tenantId, Long followUpId, Long customerId,
                             Long operatorId, String operatorName,
                             String action, String beforeData, String afterData)
    {
        CrmAuditEvent event = new CrmAuditEvent();
        event.setTenantId(tenantId);
        event.setEventType("FOLLOW_UP");
        event.setEntityType("FOLLOW_UP");
        event.setEntityId(String.valueOf(followUpId));
        event.setOperatorId(operatorId);
        event.setOperatorName(operatorName);
        event.setAction(action);
        event.setBeforeData(beforeData);
        event.setAfterData(afterData);
        auditEventService.record(event);
    }

    private void recordTimeline(String tenantId, Long customerId, Long operatorId,
                                String operatorName, String eventType, String eventData)
    {
        CrmCustomerTimeline timeline = new CrmCustomerTimeline();
        timeline.setTenantId(tenantId);
        timeline.setCustomerId(customerId);
        timeline.setEventType(eventType);
        timeline.setOperatorId(operatorId);
        timeline.setOperatorName(operatorName);
        timeline.setEventData(eventData);
        timelineService.record(timeline);
    }
}
