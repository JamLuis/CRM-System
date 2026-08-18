package com.ruoyi.crm.customer.service;

import com.ruoyi.crm.customer.domain.CrmCustomer;

import java.util.Date;

/**
 * 客户经营状态机服务接口
 * <p>
 * 经营状态流转：
 * <pre>
 *   正常 ──暂停──→ 暂停跟进 ──恢复──→ 正常
 *   正常/暂停 ──失效──→ 已失效 ──恢复──→ 正常(阶段重置为待跟进)
 *   正常/暂停/失效 ──归档──→ 已归档 ──恢复(仅管理员)──→ 正常
 * </pre>
 *
 * @author ruoyi-crm
 */
public interface CustomerStatusService
{
    /**
     * 暂停跟进
     * <p>
     * 仅主负责人可暂停。暂停后 nextFollowUpAt 可为空。
     *
     * @param customerId     客户 ID
     * @param reason         暂停原因
     * @param plannedResumeAt 计划恢复时间
     * @return 更新后的客户
     */
    CrmCustomer pause(Long customerId, String reason, Date plannedResumeAt);

    /**
     * 恢复跟进
     * <p>
     * 主负责人可恢复暂停状态。
     *
     * @param customerId 客户 ID
     * @param reason     恢复原因
     * @return 更新后的客户
     */
    CrmCustomer resume(Long customerId, String reason);

    /**
     * 设为已失效
     * <p>
     * 销售主管或管理员可执行。
     *
     * @param customerId 客户 ID
     * @param reason     失效原因
     * @return 更新后的客户
     */
    CrmCustomer invalidate(Long customerId, String reason);

    /**
     * 归档客户
     * <p>
     * 仅管理员可归档。归档后可同名重建。
     *
     * @param customerId 客户 ID
     * @param reason     归档原因
     * @return 更新后的客户
     */
    CrmCustomer archive(Long customerId, String reason);

    /**
     * 恢复归档客户
     * <p>
     * 仅管理员可恢复。恢复后状态为正常。
     *
     * @param customerId 客户 ID
     * @param reason     恢复原因
     * @return 更新后的客户
     */
    CrmCustomer restoreFromArchive(Long customerId, String reason);

    /**
     * 恢复失效客户
     * <p>
     * 销售主管或管理员可恢复。恢复后状态为正常，生命周期阶段重置为"待跟进"。
     *
     * @param customerId 客户 ID
     * @param reason     恢复原因
     * @return 更新后的客户
     */
    CrmCustomer restoreFromInvalid(Long customerId, String reason);
}
