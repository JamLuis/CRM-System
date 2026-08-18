package com.ruoyi.crm.outbox.service.impl;

import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.outbox.domain.CrmOutbox;
import com.ruoyi.crm.outbox.mapper.CrmOutboxMapper;
import com.ruoyi.crm.outbox.service.OutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Outbox 消息服务实现
 */
@Service
public class OutboxServiceImpl implements OutboxService
{
    @Autowired
    private CrmOutboxMapper outboxMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    public void create(CrmOutbox outbox)
    {
        if (outbox.getId() == null)
        {
            outbox.setId(idGenerator.nextId());
        }
        if (outbox.getStatus() == null)
        {
            outbox.setStatus("PENDING");
        }
        if (outbox.getRetryCount() == null)
        {
            outbox.setRetryCount(0);
        }
        outboxMapper.insert(outbox);
    }

    @Override
    public List<CrmOutbox> findPending(String tenantId, int limit)
    {
        return outboxMapper.selectPending(tenantId, limit);
    }

    @Override
    public List<CrmOutbox> findFailedForRetry(String tenantId, int maxRetries, int limit)
    {
        return outboxMapper.selectFailedForRetry(tenantId, maxRetries, limit);
    }

    @Override
    public boolean updateStatus(Long id, String tenantId, String status, int retryCount, Date nextRetryTime, int version)
    {
        return outboxMapper.updateStatus(id, tenantId, status, retryCount, nextRetryTime, version) > 0;
    }

    @Override
    public List<CrmOutbox> findDead(String tenantId)
    {
        return outboxMapper.selectDead(tenantId);
    }

    @Override
    public boolean resetDeadToPending(Long id, String tenantId, int version)
    {
        return outboxMapper.resetDeadToPending(id, tenantId, version) > 0;
    }
}
