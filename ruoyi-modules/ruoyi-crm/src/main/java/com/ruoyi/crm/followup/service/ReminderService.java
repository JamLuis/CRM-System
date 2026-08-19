package com.ruoyi.crm.followup.service;

import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import com.ruoyi.crm.followup.domain.CrmReminderPlan;

import java.util.Date;
import java.util.List;

/**
 * 提醒服务接口
 *
 * @author ruoyi-crm
 */
public interface ReminderService
{
    /**
     * 创建提醒计划（取消旧计划+创建新计划+创建投递任务）
     *
     * @param sourceFollowUpId 来源跟进记录ID
     * @param customerId       客户ID
     * @param plannedFollowUpAt 计划下一次跟进时间
     * @param recipientUserId  接收人用户ID
     * @param recipientName   接收人姓名
     * @return 提醒计划
     */
    CrmReminderPlan createPlan(Long sourceFollowUpId, Long customerId,
                               Date plannedFollowUpAt,
                               Long recipientUserId, String recipientName);

    /**
     * 按客户取消所有活动计划
     *
     * @param customerId 客户ID
     * @param operatorName 操作人
     */
    void cancelByCustomer(Long customerId, String operatorName);

    /**
     * 按计划ID取消
     *
     * @param planId      计划ID
     * @param operatorName 操作人
     */
    void cancelByPlan(Long planId, String operatorName);

    /**
     * 按客户查询提醒计划列表
     *
     * @param customerId 客户ID
     * @return 计划列表
     */
    List<CrmReminderPlan> listByCustomer(Long customerId);

    /**
     * 查询当前用户的待办投递（PENDING/RETRYING/SENT，联查客户名称）
     *
     * @return 待办投递列表
     */
    List<CrmReminderDelivery> listMyTodos();

    /**
     * 完成当前用户的一条待办投递
     *
     * @param deliveryId 投递ID
     * @return 更新后的投递
     */
    CrmReminderDelivery completeMyTodo(Long deliveryId);

    /**
     * 标记投递为已发送
     *
     * @param deliveryId 投递ID
     * @param operatorName 操作人
     */
    void markSent(Long deliveryId, String operatorName);

    /**
     * 标记投递为已完成
     *
     * @param deliveryId 投递ID
     * @param operatorName 操作人
     */
    void markCompleted(Long deliveryId, String operatorName);

    /**
     * 标记投递为最终失败
     *
     * @param deliveryId 投递ID
     * @param errorCode   错误码
     * @param operatorName 操作人
     */
    void markFailed(Long deliveryId, String errorCode, String operatorName);

    /**
     * 增加重试次数
     *
     * @param deliveryId 投递ID
     * @param errorCode   错误码
     * @param operatorName 操作人
     * @return 更新后的投递对象
     */
    CrmReminderDelivery incrementRetry(Long deliveryId, String errorCode, String operatorName);

    /**
     * 查询待调度投递（Worker轮询用）
     *
     * @param scheduledBefore 调度时间上限
     * @return 待调度投递列表
     */
    List<CrmReminderDelivery> listPendingForDispatch(Date scheduledBefore);
}
