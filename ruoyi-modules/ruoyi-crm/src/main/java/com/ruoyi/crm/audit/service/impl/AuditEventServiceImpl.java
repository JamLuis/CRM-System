package com.ruoyi.crm.audit.service.impl;

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
        auditEventMapper.insert(event);
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
