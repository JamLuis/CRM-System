package com.ruoyi.crm.followup.controller;

import com.ruoyi.common.core.domain.R;
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
}
