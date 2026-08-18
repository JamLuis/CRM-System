package com.ruoyi.crm.followup.service;

import com.ruoyi.crm.followup.domain.CrmFollowUp;

import java.util.List;

/**
 * 跟进记录服务接口
 *
 * @author ruoyi-crm
 */
public interface FollowUpService
{
    /**
     * 创建跟进记录
     * <p>
     * 业务规则：
     * - 跟进时间不能是未来时间，不能早于30天前
     * - PHONE/WECHAT 必须关联至少一条 AVAILABLE 图片附件
     * - 提交后正文不可修改
     * - 自动创建提醒计划
     * - 更新客户最近有效跟进时间
     *
     * @param followUp      跟进记录
     * @param contactIds    关联联系人ID列表
     * @param attachmentIds 关联附件ID列表
     * @return 保存后的跟进记录
     */
    CrmFollowUp create(CrmFollowUp followUp, List<Long> contactIds, List<Long> attachmentIds);

    /**
     * 更正跟进记录（创建一条新的更正记录，引用原记录）
     *
     * @param originalFollowUpId 原跟进记录ID
     * @param correction         更正跟进记录
     * @param contactIds         关联联系人ID列表
     * @param attachmentIds      关联附件ID列表
     * @param correctionReason   更正原因
     * @return 更正后的新跟进记录
     */
    CrmFollowUp correct(Long originalFollowUpId, CrmFollowUp correction,
                        List<Long> contactIds, List<Long> attachmentIds,
                        String correctionReason);

    /**
     * 作废跟进记录
     *
     * @param followUpId   跟进记录ID
     * @param voidedReason 作废原因
     * @return 作废后的跟进记录
     */
    CrmFollowUp void_(Long followUpId, String voidedReason);

    /**
     * 查询跟进记录详情
     *
     * @param followUpId 跟进记录ID
     * @return 跟进记录
     */
    CrmFollowUp detail(Long followUpId);

    /**
     * 按客户查询跟进记录列表
     *
     * @param customerId 客户ID
     * @return 跟进记录列表
     */
    List<CrmFollowUp> listByCustomer(Long customerId);
}
