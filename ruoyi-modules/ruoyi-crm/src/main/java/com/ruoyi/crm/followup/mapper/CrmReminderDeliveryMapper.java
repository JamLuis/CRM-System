package com.ruoyi.crm.followup.mapper;

import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 提醒投递 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmReminderDeliveryMapper
{
    /**
     * 按投递ID查询
     */
    CrmReminderDelivery selectByDeliveryId(@Param("tenantId") String tenantId,
                                           @Param("deliveryId") Long deliveryId);

    /**
     * 按计划ID查询投递列表
     */
    List<CrmReminderDelivery> selectByPlanId(@Param("tenantId") String tenantId,
                                             @Param("planId") Long planId);

    /**
     * 按客户查询投递列表
     */
    List<CrmReminderDelivery> selectByCustomer(@Param("tenantId") String tenantId,
                                                @Param("customerId") Long customerId);

    /**
     * 查询待调度/重试中的投递（Worker轮询用）
     */
    List<CrmReminderDelivery> selectPendingForDispatch(@Param("tenantId") String tenantId,
                                                       @Param("scheduledBefore") Date scheduledBefore);

    /**
     * 插入投递
     */
    int insert(CrmReminderDelivery delivery);

    /**
     * 更新状态
     */
    int updateStatus(@Param("tenantId") String tenantId,
                     @Param("deliveryId") Long deliveryId,
                     @Param("status") String status,
                     @Param("lastAttemptAt") Date lastAttemptAt,
                     @Param("lastErrorCode") String lastErrorCode,
                     @Param("completedAt") Date completedAt,
                     @Param("updateBy") String updateBy);

    /**
     * 增加重试次数
     */
    int incrementRetry(@Param("tenantId") String tenantId,
                       @Param("deliveryId") Long deliveryId,
                       @Param("lastAttemptAt") Date lastAttemptAt,
                       @Param("lastErrorCode") String lastErrorCode,
                       @Param("updateBy") String updateBy);

    /**
     * 按计划ID取消所有待调度投递
     */
    int cancelByPlanId(@Param("tenantId") String tenantId,
                        @Param("planId") Long planId,
                        @Param("updateBy") String updateBy);

    /**
     * 按客户取消所有待调度投递
     */
    int cancelByCustomer(@Param("tenantId") String tenantId,
                         @Param("customerId") Long customerId,
                         @Param("updateBy") String updateBy);
}
