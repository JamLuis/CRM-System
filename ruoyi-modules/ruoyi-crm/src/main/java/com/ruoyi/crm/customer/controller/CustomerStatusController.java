package com.ruoyi.crm.customer.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.service.CustomerStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 客户经营状态管理接口
 * <p>
 * 提供暂停、恢复、失效、归档等状态流转操作。
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/customers/{customerId}/status")
public class CustomerStatusController
{
    @Autowired
    private CustomerStatusService statusService;

    /**
     * 暂停跟进
     *
     * @param customerId      客户 ID
     * @param reason          暂停原因
     * @param plannedResumeAt 计划恢复时间
     * @return 更新后的客户
     */
    @PostMapping("/pause")
    public R<CrmCustomer> pause(@PathVariable Long customerId,
                                @RequestParam(value = "reason", required = false) String reason,
                                @RequestParam(value = "plannedResumeAt", required = false)
                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date plannedResumeAt)
    {
        return R.ok(statusService.pause(customerId, reason, plannedResumeAt));
    }

    /**
     * 恢复跟进
     *
     * @param customerId 客户 ID
     * @param reason     恢复原因
     * @return 更新后的客户
     */
    @PostMapping("/resume")
    public R<CrmCustomer> resume(@PathVariable Long customerId,
                                @RequestParam(value = "reason", required = false) String reason)
    {
        return R.ok(statusService.resume(customerId, reason));
    }

    /**
     * 设为已失效
     *
     * @param customerId 客户 ID
     * @param reason     失效原因
     * @return 更新后的客户
     */
    @PostMapping("/invalidate")
    public R<CrmCustomer> invalidate(@PathVariable Long customerId,
                                    @RequestParam(value = "reason", required = false) String reason)
    {
        return R.ok(statusService.invalidate(customerId, reason));
    }

    /**
     * 归档客户
     *
     * @param customerId 客户 ID
     * @param reason     归档原因
     * @return 更新后的客户
     */
    @PostMapping("/archive")
    public R<CrmCustomer> archive(@PathVariable Long customerId,
                                 @RequestParam(value = "reason", required = false) String reason)
    {
        return R.ok(statusService.archive(customerId, reason));
    }

    /**
     * 恢复归档客户（仅管理员）
     *
     * @param customerId 客户 ID
     * @param reason     恢复原因
     * @return 更新后的客户
     */
    @PostMapping("/restore-archive")
    public R<CrmCustomer> restoreFromArchive(@PathVariable Long customerId,
                                            @RequestParam(value = "reason", required = false) String reason)
    {
        return R.ok(statusService.restoreFromArchive(customerId, reason));
    }

    /**
     * 恢复失效客户（销售主管或管理员）
     *
     * @param customerId 客户 ID
     * @param reason     恢复原因
     * @return 更新后的客户
     */
    @PostMapping("/restore-invalid")
    public R<CrmCustomer> restoreFromInvalid(@PathVariable Long customerId,
                                            @RequestParam(value = "reason", required = false) String reason)
    {
        return R.ok(statusService.restoreFromInvalid(customerId, reason));
    }
}
