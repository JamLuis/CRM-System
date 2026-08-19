package com.ruoyi.crm.followup.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import com.ruoyi.crm.followup.domain.CrmReminderPlan;
import com.ruoyi.crm.followup.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提醒管理接口
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/reminders")
public class ReminderController
{
    @Autowired
    private ReminderService reminderService;

    /**
     * 按客户查询提醒计划列表
     *
     * @param customerId 客户ID
     * @return 计划列表
     */
    @GetMapping("/by-customer/{customerId}")
    public R<List<CrmReminderPlan>> listByCustomer(@PathVariable Long customerId)
    {
        return R.ok(reminderService.listByCustomer(customerId));
    }

    /**
     * 按客户取消所有活动计划
     *
     * @param customerId 客户ID
     * @return 操作结果
     */
    @PostMapping("/by-customer/{customerId}/cancel")
    public R<Void> cancelByCustomer(@PathVariable Long customerId)
    {
        reminderService.cancelByCustomer(customerId, "manual");
        return R.ok();
    }

    /**
     * 查询我的待办（当前用户的 PENDING/RETRYING/SENT 投递，联查客户名称）
     *
     * @return 待办列表
     */
    @GetMapping("/my-todos")
    public R<List<CrmReminderDelivery>> listMyTodos()
    {
        return R.ok(reminderService.listMyTodos());
    }

    /**
     * 完成我的一条待办
     *
     * @param deliveryId 投递ID
     * @return 更新后的投递
     */
    @PostMapping("/my-todos/{deliveryId}/complete")
    public R<CrmReminderDelivery> completeMyTodo(@PathVariable Long deliveryId)
    {
        return R.ok(reminderService.completeMyTodo(deliveryId));
    }
}
