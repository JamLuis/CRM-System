package com.ruoyi.crm.followup.mapper;

import com.ruoyi.crm.followup.domain.CrmFollowUpStatusStrategy;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 跟进状态策略 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmFollowUpStatusStrategyMapper
{
    /**
     * 查询租户当前生效策略
     */
    CrmFollowUpStatusStrategy selectActive(@Param("tenantId") String tenantId);

    /**
     * 按策略ID查询
     */
    CrmFollowUpStatusStrategy selectByStrategyId(@Param("tenantId") String tenantId,
                                                  @Param("strategyId") Long strategyId);

    /**
     * 查询全部策略
     */
    List<CrmFollowUpStatusStrategy> selectAll(@Param("tenantId") String tenantId);

    /**
     * 插入策略
     */
    int insert(CrmFollowUpStatusStrategy strategy);

    /**
     * 停用旧策略
     */
    int deactivateOld(@Param("tenantId") String tenantId,
                      @Param("excludeStrategyId") Long excludeStrategyId,
                      @Param("updateBy") String updateBy);
}
