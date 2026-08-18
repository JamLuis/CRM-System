package com.ruoyi.crm.audit.mapper;

import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户时间线 Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmCustomerTimelineMapper
{
    /**
     * 新增时间线事件
     */
    int insert(CrmCustomerTimeline timeline);

    /**
     * 根据客户 ID 查询时间线
     */
    List<CrmCustomerTimeline> selectByCustomerId(@Param("tenantId") String tenantId,
                                                  @Param("customerId") Long customerId);
}
