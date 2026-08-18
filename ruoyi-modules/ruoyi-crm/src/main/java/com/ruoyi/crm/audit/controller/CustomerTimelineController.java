package com.ruoyi.crm.audit.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 客户动态时间线接口（只读）
 * <p>
 * 权限校验复用 {@link CustomerService#detail(Long)}：只有具备客户读权限
 * （CRM_CUSTOMER_READ + 数据范围）的用户才能查看该客户的动态。
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/customers")
public class CustomerTimelineController
{
    @Autowired
    private CustomerTimelineService timelineService;

    @Autowired
    private CustomerService customerService;

    /**
     * 查询客户动态时间线（只读，按时间倒序）
     *
     * @param customerId 客户 ID
     * @return 时间线事件列表
     */
    @GetMapping("/{customerId}/timeline")
    public R<List<CrmCustomerTimeline>> listByCustomer(@PathVariable Long customerId)
    {
        // 先校验客户存在性与读权限（不存在或无权限时抛异常）
        customerService.detail(customerId);

        String tenantId = TenantContext.getTenantId();
        return R.ok(timelineService.findByCustomer(tenantId, customerId));
    }
}
