package com.ruoyi.crm.common.mapper;

import com.ruoyi.crm.common.domain.CrmIdempotencyKey;
import org.apache.ibatis.annotations.Param;

/**
 * 幂等键 Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmIdempotencyKeyMapper
{
    /**
     * 根据幂等键查询
     */
    CrmIdempotencyKey selectByKey(@Param("tenantId") String tenantId,
                                  @Param("idempotencyKey") String idempotencyKey);

    /**
     * 新增
     */
    int insert(CrmIdempotencyKey key);

    /**
     * 更新响应
     */
    int updateResponse(@Param("id") Long id,
                       @Param("tenantId") String tenantId,
                       @Param("responseStatus") int responseStatus,
                       @Param("responseBody") String responseBody);

    /**
     * 清理过期记录
     */
    int deleteExpired();
}
