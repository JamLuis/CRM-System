package com.ruoyi.crm.customer.mapper;

import com.ruoyi.crm.customer.domain.CrmCustomer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmCustomerMapper
{
    /**
     * 按客户ID查询
     */
    CrmCustomer selectByCustomerId(@Param("tenantId") String tenantId, @Param("customerId") Long customerId);

    /**
     * 按租户和规范化名称查询（重名校验）
     */
    CrmCustomer selectByActiveNameKey(@Param("tenantId") String tenantId, @Param("activeNameKey") String activeNameKey);

    /**
     * 按主负责人查询客户列表
     */
    List<CrmCustomer> selectByOwner(@Param("tenantId") String tenantId, @Param("primaryOwnerId") Long primaryOwnerId);

    /**
     * 按部门查询客户列表
     */
    List<CrmCustomer> selectByDept(@Param("tenantId") String tenantId, @Param("ownerDeptId") Long ownerDeptId);

    /**
     * 查询全部客户（管理员）
     */
    List<CrmCustomer> selectAll(@Param("tenantId") String tenantId);

    /**
     * 条件查询客户列表
     */
    List<CrmCustomer> selectList(@Param("tenantId") String tenantId, @Param("customer") CrmCustomer query);

    /**
     * 插入客户
     */
    int insert(CrmCustomer customer);

    /**
     * 更新客户（乐观锁）
     */
    int update(CrmCustomer customer);

    /**
     * 更新经营状态（乐观锁）
     */
    int updateOperatingStatus(@Param("tenantId") String tenantId,
                              @Param("customerId") Long customerId,
                              @Param("operatingStatus") String operatingStatus,
                              @Param("statusChangeReason") String statusChangeReason,
                              @Param("plannedResumeAt") java.util.Date plannedResumeAt,
                              @Param("archivedAt") java.util.Date archivedAt,
                              @Param("updateBy") String updateBy,
                              @Param("version") Integer version);

    /**
     * 更新主负责人（乐观锁）
     */
    int updatePrimaryOwner(@Param("tenantId") String tenantId,
                           @Param("customerId") Long customerId,
                           @Param("primaryOwnerId") Long primaryOwnerId,
                           @Param("primaryOwnerName") String primaryOwnerName,
                           @Param("ownerDeptId") Long ownerDeptId,
                           @Param("collaboratorIds") String collaboratorIds,
                           @Param("updateBy") String updateBy,
                           @Param("version") Integer version);
}
