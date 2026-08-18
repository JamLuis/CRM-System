package com.ruoyi.crm.customer.service;

import com.ruoyi.crm.customer.domain.CrmContact;

import java.util.List;

/**
 * 客户联系人服务接口
 * <p>
 * 业务规则：
 * <ul>
 *   <li>同客户手机号规范化唯一</li>
 *   <li>被引用的联系人只能停用不能删除</li>
 *   <li>暂停/失效/归档客户不得新增联系人</li>
 *   <li>手机号脱敏存储</li>
 * </ul>
 *
 * @author ruoyi-crm
 */
public interface ContactService
{
    /**
     * 创建联系人
     *
     * @param contact 联系人信息
     * @return 创建后的联系人
     */
    CrmContact create(CrmContact contact);

    /**
     * 编辑联系人
     *
     * @param contact 联系人信息（须携带 version）
     * @return 更新后的联系人
     */
    CrmContact edit(CrmContact contact);

    /**
     * 停用联系人
     *
     * @param contactId 联系人 ID
     * @return 更新后的联系人
     */
    CrmContact deactivate(Long contactId);

    /**
     * 查询联系人详情
     *
     * @param contactId 联系人 ID
     * @return 联系人信息
     */
    CrmContact detail(Long contactId);

    /**
     * 查询客户联系人列表
     *
     * @param customerId 客户 ID
     * @return 联系人列表
     */
    List<CrmContact> listByCustomer(Long customerId);
}
