package com.ruoyi.crm.followup.service.impl;

import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import com.ruoyi.crm.followup.domain.CrmReminderPlan;
import com.ruoyi.crm.followup.domain.ReminderDeliveryStatus;
import com.ruoyi.crm.followup.domain.ReminderPlanStatus;
import com.ruoyi.crm.followup.mapper.CrmReminderDeliveryMapper;
import com.ruoyi.crm.followup.mapper.CrmReminderPlanMapper;
import com.ruoyi.crm.followup.service.ReminderService;
import com.ruoyi.common.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 提醒服务实现
 *
 * @author ruoyi-crm
 */
@Service
public class ReminderServiceImpl implements ReminderService
{
    private static final Logger log = LoggerFactory.getLogger(ReminderServiceImpl.class);

    @Autowired
    private CrmReminderPlanMapper planMapper;

    @Autowired
    private CrmReminderDeliveryMapper deliveryMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmReminderPlan createPlan(Long sourceFollowUpId, Long customerId,
                                      Date plannedFollowUpAt,
                                      Long recipientUserId, String recipientName)
    {
        String tenantId = TenantContext.getTenantId();
        String operatorName = "system";

        // 取消客户当前活动计划
        planMapper.cancelActiveByCustomer(tenantId, customerId, operatorName);
        // 取消所有待调度投递
        deliveryMapper.cancelByCustomer(tenantId, customerId, operatorName);

        // 计算提醒发送时间：计划跟进日前1天 09:00 CST
        Date scheduledAt = calculateScheduledAt(plannedFollowUpAt);

        // 创建新计划
        CrmReminderPlan plan = new CrmReminderPlan();
        plan.setPlanId(idGenerator.nextId());
        plan.setTenantId(tenantId);
        plan.setCustomerId(customerId);
        plan.setSourceFollowUpId(sourceFollowUpId);
        plan.setPlanKey("FU-" + sourceFollowUpId);
        plan.setPlannedFollowUpAt(plannedFollowUpAt);
        plan.setScheduledAt(scheduledAt);
        plan.setStatus(ReminderPlanStatus.ACTIVE.name());
        plan.setVersion(0);
        plan.setDelFlag("0");
        plan.setCreateBy(operatorName);
        plan.setUpdateBy(operatorName);

        planMapper.insert(plan);

        // 创建投递任务
        CrmReminderDelivery delivery = new CrmReminderDelivery();
        delivery.setDeliveryId(idGenerator.nextId());
        delivery.setTenantId(tenantId);
        delivery.setPlanId(plan.getPlanId());
        delivery.setCustomerId(customerId);
        delivery.setPlanKey(plan.getPlanKey());
        delivery.setPlannedFollowUpAt(plannedFollowUpAt);
        delivery.setScheduledAt(scheduledAt);
        delivery.setRecipientUserId(recipientUserId);
        delivery.setRecipientName(recipientName);
        delivery.setStatus(ReminderDeliveryStatus.PENDING.name());
        delivery.setRetryCount(0);
        delivery.setVersion(0);
        delivery.setDelFlag("0");
        delivery.setCreateBy(operatorName);
        delivery.setUpdateBy(operatorName);

        deliveryMapper.insert(delivery);

        log.info("Reminder plan created: tenantId={}, planId={}, customerId={}, scheduledAt={}",
                tenantId, plan.getPlanId(), customerId, scheduledAt);

        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByCustomer(Long customerId, String operatorName)
    {
        String tenantId = TenantContext.getTenantId();
        planMapper.cancelActiveByCustomer(tenantId, customerId, operatorName);
        deliveryMapper.cancelByCustomer(tenantId, customerId, operatorName);

        log.info("Reminders cancelled by customer: tenantId={}, customerId={}", tenantId, customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByPlan(Long planId, String operatorName)
    {
        String tenantId = TenantContext.getTenantId();
        planMapper.cancelByPlanId(tenantId, planId, operatorName);
        deliveryMapper.cancelByPlanId(tenantId, planId, operatorName);

        log.info("Reminders cancelled by plan: tenantId={}, planId={}", tenantId, planId);
    }

    @Override
    public List<CrmReminderPlan> listByCustomer(Long customerId)
    {
        String tenantId = TenantContext.getTenantId();
        return planMapper.selectByCustomer(tenantId, customerId);
    }

    @Override
    public List<CrmReminderDelivery> listMyTodos()
    {
        String tenantId = TenantContext.getTenantId();
        Long userId = SecurityUtils.getUserId();
        return deliveryMapper.selectByRecipient(tenantId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmReminderDelivery completeMyTodo(Long deliveryId)
    {
        String tenantId = TenantContext.getTenantId();
        Long userId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();

        CrmReminderDelivery delivery = deliveryMapper.selectByDeliveryId(tenantId, deliveryId);
        if (delivery == null)
        {
            throw new IllegalArgumentException("待办不存在：" + deliveryId);
        }
        if (!userId.equals(delivery.getRecipientUserId()))
        {
            throw new IllegalArgumentException("无权操作他人的待办");
        }
        if (ReminderDeliveryStatus.COMPLETED.name().equals(delivery.getStatus()))
        {
            return delivery;
        }
        if (!ReminderDeliveryStatus.PENDING.name().equals(delivery.getStatus())
                && !ReminderDeliveryStatus.RETRYING.name().equals(delivery.getStatus())
                && !ReminderDeliveryStatus.SENT.name().equals(delivery.getStatus()))
        {
            throw new IllegalStateException("待办状态不允许完成：" + delivery.getStatus());
        }

        deliveryMapper.updateStatus(tenantId, deliveryId,
                ReminderDeliveryStatus.COMPLETED.name(), null, null, new Date(), operatorName);

        log.info("My todo completed: tenantId={}, deliveryId={}, userId={}", tenantId, deliveryId, userId);
        return deliveryMapper.selectByDeliveryId(tenantId, deliveryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSent(Long deliveryId, String operatorName)
    {
        String tenantId = TenantContext.getTenantId();
        deliveryMapper.updateStatus(tenantId, deliveryId,
                ReminderDeliveryStatus.SENT.name(), new Date(), null, null, operatorName);

        log.info("Delivery marked sent: tenantId={}, deliveryId={}", tenantId, deliveryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCompleted(Long deliveryId, String operatorName)
    {
        String tenantId = TenantContext.getTenantId();
        deliveryMapper.updateStatus(tenantId, deliveryId,
                ReminderDeliveryStatus.COMPLETED.name(), null, null, new Date(), operatorName);

        log.info("Delivery marked completed: tenantId={}, deliveryId={}", tenantId, deliveryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long deliveryId, String errorCode, String operatorName)
    {
        String tenantId = TenantContext.getTenantId();
        deliveryMapper.updateStatus(tenantId, deliveryId,
                ReminderDeliveryStatus.FAILED.name(), new Date(), errorCode, null, operatorName);

        log.warn("Delivery marked failed: tenantId={}, deliveryId={}, errorCode={}",
                tenantId, deliveryId, errorCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmReminderDelivery incrementRetry(Long deliveryId, String errorCode, String operatorName)
    {
        String tenantId = TenantContext.getTenantId();
        int rows = deliveryMapper.incrementRetry(tenantId, deliveryId, new Date(), errorCode, operatorName);
        if (rows == 0)
        {
            // 重试次数已达上限，标记为最终失败
            deliveryMapper.updateStatus(tenantId, deliveryId,
                    ReminderDeliveryStatus.FAILED.name(), new Date(), errorCode, null, operatorName);
            log.warn("Delivery retry limit reached, marking as FAILED: tenantId={}, deliveryId={}",
                    tenantId, deliveryId);
        }

        return deliveryMapper.selectByDeliveryId(tenantId, deliveryId);
    }

    @Override
    public List<CrmReminderDelivery> listPendingForDispatch(Date scheduledBefore)
    {
        String tenantId = TenantContext.getTenantId();
        return deliveryMapper.selectPendingForDispatch(tenantId, scheduledBefore);
    }

    // ==================== Private helpers ====================

    /**
     * 计算提醒发送时间：计划跟进日前1天 09:00 CST
     * 如果已过发送时间，则立即发送
     */
    private Date calculateScheduledAt(Date plannedFollowUpAt)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(plannedFollowUpAt);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date scheduledAt = cal.getTime();
        Date now = new Date();

        // 如果已过发送时间，立即发送
        if (scheduledAt.before(now))
        {
            return now;
        }

        return scheduledAt;
    }
}
