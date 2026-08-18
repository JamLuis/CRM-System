package com.ruoyi.crm.audit.service;

import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;

import java.util.List;

/**
 * 客户时间线服务接口
 */
public interface CustomerTimelineService
{
    /**
     * 记录客户时间线事件（在调用方事务内执行）
     *
     * @param timeline 时间线事件（id 由服务层赋值）
     */
    void record(CrmCustomerTimeline timeline);

    /**
     * 按客户查询时间线
     */
    List<CrmCustomerTimeline> findByCustomer(String tenantId, Long customerId);
}
