package com.ruoyi.crm.customer.service;

import com.ruoyi.crm.customer.domain.CrmCustomerOwner;
import com.ruoyi.crm.customer.domain.CrmOwnerChange;

import java.util.List;

/**
 * 客户成员维护与移交服务接口
 * <p>
 * 提供主负责人移交（乐观锁并发控制）、协同人增减等能力。
 *
 * @author ruoyi-crm
 */
public interface OwnerService
{
    /**
     * 移交主负责人
     * <p>
     * 业务规则：
     * <ul>
     *   <li>乐观锁：并发移交只有一个成功</li>
     *   <li>移交后原负责人可选择保留为协同人</li>
     *   <li>记录 crm_owner_change 不可变记录</li>
     *   <li>更新 crm_customer_owner 关系表</li>
     * </ul>
     *
     * @param customerId               客户 ID
     * @param targetOwnerId            新主负责人用户 ID
     * @param targetOwnerName          新主负责人姓名
     * @param targetOwnerDeptId        新主负责人部门 ID
     * @param keepPreviousAsCollaborator 是否保留原负责人为协同人
     * @param reason                   移交原因
     * @return 变更记录
     */
    CrmOwnerChange transfer(Long customerId, Long targetOwnerId, String targetOwnerName,
                            Long targetOwnerDeptId, boolean keepPreviousAsCollaborator, String reason);

    /**
     * 分配主负责人（新建客户时由 CustomerService 内部调用）
     * <p>
     * 此方法为内部方法，不直接暴露给 Controller。
     *
     * @param customerId        客户 ID
     * @param ownerId           主负责人用户 ID
     * @param ownerName         主负责人姓名
     * @param ownerDeptId      主负责人部门 ID
     * @param operatorId       操作人 ID
     * @param operatorName     操作人姓名
     * @return 变更记录
     */
    CrmOwnerChange assign(Long customerId, Long ownerId, String ownerName,
                          Long ownerDeptId, Long operatorId, String operatorName);

    /**
     * 新增协同人
     *
     * @param customerId      客户 ID
     * @param collaboratorId  协同人用户 ID
     * @param collaboratorName 协同人姓名
     * @return 变更记录
     */
    CrmOwnerChange addCollaborator(Long customerId, Long collaboratorId, String collaboratorName);

    /**
     * 移除协同人
     *
     * @param customerId      客户 ID
     * @param collaboratorId  协同人用户 ID
     * @return 变更记录
     */
    CrmOwnerChange removeCollaborator(Long customerId, Long collaboratorId);

    /**
     * 查询客户成员列表
     *
     * @param customerId 客户 ID
     * @return ACTIVE 成员列表
     */
    List<CrmCustomerOwner> listMembers(Long customerId);

    /**
     * 查询客户负责人变更历史
     *
     * @param customerId 客户 ID
     * @return 变更记录列表
     */
    List<CrmOwnerChange> listChangeHistory(Long customerId);
}
