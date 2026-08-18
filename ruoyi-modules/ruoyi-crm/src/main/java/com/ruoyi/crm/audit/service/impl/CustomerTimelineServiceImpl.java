package com.ruoyi.crm.audit.service.impl;

import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;
import com.ruoyi.crm.audit.mapper.CrmCustomerTimelineMapper;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.id.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户时间线服务实现
 */
@Service
public class CustomerTimelineServiceImpl implements CustomerTimelineService
{
    @Autowired
    private CrmCustomerTimelineMapper timelineMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    public void record(CrmCustomerTimeline timeline)
    {
        if (timeline.getId() == null)
        {
            timeline.setId(idGenerator.nextId());
        }
        timelineMapper.insert(timeline);
    }

    @Override
    public List<CrmCustomerTimeline> findByCustomer(String tenantId, Long customerId)
    {
        return timelineMapper.selectByCustomerId(tenantId, customerId);
    }
}
