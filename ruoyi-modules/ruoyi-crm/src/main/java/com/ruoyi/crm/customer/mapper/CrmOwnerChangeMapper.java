package com.ruoyi.crm.customer.mapper;

import com.ruoyi.crm.customer.domain.CrmOwnerChange;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 负责人变更记录 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmOwnerChangeMapper
{
    /**
     * 插入变更记录（不可变表）
     */
    int insert(CrmOwnerChange change);

    /**
     * 按客户查询变更历史
     */
    List<CrmOwnerChange> selectByCustomer(@Param("tenantId") String tenantId, @Param("customerId") Long customerId);
}
