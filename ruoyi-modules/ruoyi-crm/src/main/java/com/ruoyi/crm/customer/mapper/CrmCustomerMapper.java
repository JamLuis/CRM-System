package com.ruoyi.crm.customer.mapper;

import com.ruoyi.crm.customer.domain.CrmCustomer;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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

    List<CrmCustomer> selectVisibleList(@Param("tenantId") String tenantId,
                                        @Param("customer") CrmCustomer query,
                                        @Param("scopeType") String scopeType,
                                        @Param("operatorId") Long operatorId,
                                        @Param("operatorDeptId") Long operatorDeptId);

    /**
     * 插入客户
     */
    int insert(CrmCustomer customer);

    /**
     * 重新导入时仅补充历史来源字段，不覆盖 CRM 内已维护的负责人和业务资料。
     */
    int updateImportedMetadata(@Param("tenantId") String tenantId,
                               @Param("customerId") Long customerId,
                               @Param("customer") CrmCustomer customer);

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

    /**
     * 更新跟进状态（乐观锁）
     */
    int updateFollowUpStatus(@Param("tenantId") String tenantId,
                             @Param("customerId") Long customerId,
                             @Param("followUpStatus") String followUpStatus,
                             @Param("updateBy") String updateBy,
                             @Param("version") Integer version);

    /** 导入历史跟进后，按实际记录批量刷新客户最近有效跟进时间。 */
    int refreshLastEffectiveFollowUpAt(@Param("tenantId") String tenantId,
                                       @Param("updateBy") String updateBy);

    /**
     * 按跟进状态查询客户ID列表（批量重算用）
     */
    java.util.List<CrmCustomer> selectIdsByFollowUpStatus(@Param("tenantId") String tenantId,
                                                           @Param("followUpStatus") String followUpStatus);

    /**
     * 更新客户跟进时间戳（乐观锁）：最近有效跟进时间取较大值，下次跟进时间仅当非空时覆盖
     */
    int updateFollowUpTimestamps(@Param("tenantId") String tenantId,
                                 @Param("customerId") Long customerId,
                                 @Param("lastEffectiveFollowUpAt") Date lastEffectiveFollowUpAt,
                                 @Param("nextFollowUpAt") Date nextFollowUpAt,
                                 @Param("updateBy") String updateBy,
                                 @Param("version") Integer version);
}
