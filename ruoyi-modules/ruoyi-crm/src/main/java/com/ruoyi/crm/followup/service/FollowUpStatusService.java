package com.ruoyi.crm.followup.service;

import com.ruoyi.crm.followup.domain.CrmFollowUpStatusStrategy;

import java.util.List;

/**
 * 跟进状态（健康度）服务接口
 *
 * @author ruoyi-crm
 */
public interface FollowUpStatusService
{
    /**
     * 计算单个客户的跟进状态
     *
     * @param customerId 客户ID
     * @return 计算后的状态
     */
    String calculate(Long customerId);

    /**
     * 批量重算所有 NORMAL 状态客户（定时任务调用）
     *
     * @return 重算的客户数量
     */
    int recalculateBatch();

    /**
     * 保存策略（停用旧策略+创建新策略+触发重算）
     *
     * @param strategy 策略
     * @return 保存后的策略
     */
    CrmFollowUpStatusStrategy saveStrategy(CrmFollowUpStatusStrategy strategy);

    /**
     * 查询当前生效策略
     *
     * @return 策略
     */
    CrmFollowUpStatusStrategy getActiveStrategy();

    /**
     * 查询全部策略
     *
     * @return 策略列表
     */
    List<CrmFollowUpStatusStrategy> listStrategies();
}
