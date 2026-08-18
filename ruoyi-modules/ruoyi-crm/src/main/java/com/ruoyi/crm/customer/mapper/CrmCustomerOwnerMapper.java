package com.ruoyi.crm.customer.mapper;

import com.ruoyi.crm.customer.domain.CrmCustomerOwner;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户成员关系 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmCustomerOwnerMapper
{
    /**
     * 按客户查询所有 ACTIVE 成员
     */
    List<CrmCustomerOwner> selectActiveByCustomer(@Param("tenantId") String tenantId, @Param("customerId") Long customerId);

    /**
     * 查询客户的 ACTIVE 主负责人
     */
    CrmCustomerOwner selectActivePrimary(@Param("tenantId") String tenantId, @Param("customerId") Long customerId);

    /**
     * 查询客户的 ACTIVE 协同人列表
     */
    List<CrmCustomerOwner> selectActiveCollaborators(@Param("tenantId") String tenantId, @Param("customerId") Long customerId);

    /**
     * 按用户查询其 ACTIVE 主负责的客户成员关系
     */
    List<CrmCustomerOwner> selectByUserAsPrimary(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 按用户查询其 ACTIVE 协同的客户成员关系
     */
    List<CrmCustomerOwner> selectByUserAsCollaborator(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 插入成员关系
     */
    int insert(CrmCustomerOwner owner);

    /**
     * 将客户某角色的 ACTIVE 成员置为 INACTIVE
     */
    int deactivateByCustomerAndRole(@Param("tenantId") String tenantId,
                                    @Param("customerId") Long customerId,
                                    @Param("roleType") String roleType,
                                    @Param("updateBy") String updateBy);

    /**
     * 将指定用户在某客户的协同关系置为 INACTIVE
     */
    int deactivateCollaborator(@Param("tenantId") String tenantId,
                               @Param("customerId") Long customerId,
                               @Param("userId") Long userId,
                               @Param("updateBy") String updateBy);
}
