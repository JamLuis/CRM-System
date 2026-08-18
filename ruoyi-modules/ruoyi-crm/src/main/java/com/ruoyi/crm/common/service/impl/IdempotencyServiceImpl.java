package com.ruoyi.crm.common.service.impl;

import com.ruoyi.crm.common.domain.CrmIdempotencyKey;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.mapper.CrmIdempotencyKeyMapper;
import com.ruoyi.crm.common.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 幂等键服务实现
 */
@Service
public class IdempotencyServiceImpl implements IdempotencyService
{
    @Autowired
    private CrmIdempotencyKeyMapper idempotencyKeyMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    public CrmIdempotencyKey findByKey(String tenantId, String idempotencyKey)
    {
        return idempotencyKeyMapper.selectByKey(tenantId, idempotencyKey);
    }

    @Override
    public void create(CrmIdempotencyKey idempotencyKey)
    {
        if (idempotencyKey.getId() == null)
        {
            idempotencyKey.setId(idGenerator.nextId());
        }
        idempotencyKeyMapper.insert(idempotencyKey);
    }

    @Override
    public void updateResponse(Long id, String tenantId, int responseStatus, String responseBody)
    {
        idempotencyKeyMapper.updateResponse(id, tenantId, responseStatus, responseBody);
    }

    @Override
    public int deleteExpired()
    {
        return idempotencyKeyMapper.deleteExpired();
    }
}
