package com.ruoyi.crm.followup.mapper;

import com.ruoyi.crm.followup.domain.CrmFollowUp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 跟进记录 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmFollowUpMapper
{
    /**
     * 按跟进ID查询
     */
    CrmFollowUp selectByFollowUpId(@Param("tenantId") String tenantId,
                                   @Param("followUpId") Long followUpId);

    /**
     * 按客户查询有效跟进列表（排除已作废）
     */
    List<CrmFollowUp> selectByCustomer(@Param("tenantId") String tenantId,
                                       @Param("customerId") Long customerId);

    /**
     * 查询客户最近一条有效跟进
     */
    CrmFollowUp selectLastEffectiveByCustomer(@Param("tenantId") String tenantId,
                                               @Param("customerId") Long customerId);

    /**
     * 插入跟进记录
     */
    int insert(CrmFollowUp followUp);

    /**
     * 标记原记录已被更正
     */
    int markCorrected(@Param("tenantId") String tenantId,
                      @Param("followUpId") Long followUpId);

    /**
     * 作废跟进记录
     */
    int markVoided(@Param("tenantId") String tenantId,
                   @Param("followUpId") Long followUpId,
                   @Param("voidedReason") String voidedReason,
                   @Param("updateBy") String updateBy);
}
