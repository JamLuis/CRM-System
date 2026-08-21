package com.ruoyi.crm.audit.service.impl;

import com.alibaba.fastjson2.JSON;
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
        timeline.setEventData(normalizeJson(timeline.getEventData()));
        timelineMapper.insert(timeline);
    }

    /**
     * 时间线事件列为 JSON；普通文本转换成合法 JSON 字符串。
     */
    private String normalizeJson(String value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            JSON.parse(value);
            return value;
        }
        catch (Exception ignored)
        {
            return JSON.toJSONString(value);
        }
    }

    @Override
    public List<CrmCustomerTimeline> findByCustomer(String tenantId, Long customerId)
    {
        return timelineMapper.selectByCustomerId(tenantId, customerId);
    }
}
