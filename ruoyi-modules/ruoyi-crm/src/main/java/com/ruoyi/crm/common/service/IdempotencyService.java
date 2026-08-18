package com.ruoyi.crm.common.service;

import com.ruoyi.crm.common.domain.CrmIdempotencyKey;

/**
 * 幂等键服务接口
 */
public interface IdempotencyService
{
    /**
     * 查询已缓存的幂等响应
     *
     * @return 已缓存的记录，不存在则返回 null
     */
    CrmIdempotencyKey findByKey(String tenantId, String idempotencyKey);

    /**
     * 创建幂等键记录（请求开始时）
     *
     * @param idempotencyKey 幂等键实体（id 由服务层赋值）
     */
    void create(CrmIdempotencyKey idempotencyKey);

    /**
     * 更新幂等响应（请求完成后）
     */
    void updateResponse(Long id, String tenantId, int responseStatus, String responseBody);

    /**
     * 清理过期记录
     */
    int deleteExpired();
}
