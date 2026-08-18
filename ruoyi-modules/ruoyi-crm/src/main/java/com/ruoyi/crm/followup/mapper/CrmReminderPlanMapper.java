package com.ruoyi.crm.followup.mapper;

import com.ruoyi.crm.followup.domain.CrmReminderPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 提醒计划 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmReminderPlanMapper
{
    /**
     * 按计划ID查询
     */
    CrmReminderPlan selectByPlanId(@Param("tenantId") String tenantId,
                                   @Param("planId") Long planId);

    /**
     * 查询客户当前活动计划
     */
    CrmReminderPlan selectActiveByCustomer(@Param("tenantId") String tenantId,
                                           @Param("customerId") Long customerId);

    /**
     * 按客户查询全部计划
     */
    List<CrmReminderPlan> selectByCustomer(@Param("tenantId") String tenantId,
                                           @Param("customerId") Long customerId);

    /**
     * 插入计划
     */
    int insert(CrmReminderPlan plan);

    /**
     * 取消客户当前活动计划
     */
    int cancelActiveByCustomer(@Param("tenantId") String tenantId,
                               @Param("customerId") Long customerId,
                               @Param("updateBy") String updateBy);

    /**
     * 按计划ID取消
     */
    int cancelByPlanId(@Param("tenantId") String tenantId,
                       @Param("planId") Long planId,
                       @Param("updateBy") String updateBy);
}
