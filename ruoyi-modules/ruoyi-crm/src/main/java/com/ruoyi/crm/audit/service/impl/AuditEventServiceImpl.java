package com.ruoyi.crm.audit.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import com.ruoyi.crm.audit.mapper.CrmAuditEventMapper;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.common.id.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 审计事件服务实现
 * <p>
 * 所有方法在调用方事务内执行，不独立开事务。
 */
@Service
public class AuditEventServiceImpl implements AuditEventService
{
    @Autowired
    private CrmAuditEventMapper auditEventMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    public void record(CrmAuditEvent event)
    {
        if (event.getId() == null)
        {
            event.setId(idGenerator.nextId());
        }
        event.setBeforeData(normalizeJson(event.getBeforeData()));
        event.setAfterData(normalizeJson(event.getAfterData()));
        auditEventMapper.insert(event);
    }

    /**
     * 数据库列为 JSON；兼容历史调用方传入的普通文本，同时保留已经合法的 JSON。
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
    public List<CrmAuditEvent> findByEntity(String tenantId, String entityType, String entityId)
    {
        return auditEventMapper.selectByEntity(tenantId, entityType, entityId);
    }

    @Override
    public List<CrmAuditEvent> findByOperator(String tenantId, Long operatorId)
    {
        return auditEventMapper.selectByOperator(tenantId, operatorId);
    }

    @Override
    public List<CrmAuditEvent> findByTimeRange(String tenantId, Date startTime, Date endTime)
    {
        return auditEventMapper.selectByTimeRange(tenantId, startTime, endTime);
    }
}
