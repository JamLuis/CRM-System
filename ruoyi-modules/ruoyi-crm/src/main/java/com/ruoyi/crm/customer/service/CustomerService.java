package com.ruoyi.crm.customer.service;

import com.ruoyi.crm.customer.domain.CrmCustomer;

import java.util.List;

/**
 * 客户服务接口
 * <p>
 * 提供客户创建、查询、编辑、列表等核心 CRUD 能力。
 * 所有方法在内部完成权限校验和租户上下文绑定。
 *
 * @author ruoyi-crm
 */
public interface CustomerService
{
    /**
     * 创建客户
     * <p>
     * 业务规则：
     * <ul>
     *   <li>重名硬查重：同租户下 active_name_key 唯一（已归档客户除外）</li>
     *   <li>初始经营状态为"正常"，生命周期阶段为"新获取"</li>
     *   <li>正常客户必须有 nextFollowUpAt</li>
     *   <li>创建时同时写入 crm_customer_owner 主负责人记录</li>
     * </ul>
     *
     * @param customer 客户信息（customerId/version 由服务层赋值）
     * @return 创建后的客户对象
     */
    CrmCustomer create(CrmCustomer customer);

    /**
     * 查询客户详情
     *
     * @param customerId 客户 ID
     * @return 客户对象
     */
    CrmCustomer detail(Long customerId);

    /**
     * 编辑客户核心字段
     * <p>
     * 仅允许修改名称、地址、标签、重要程度、来源、行业、备注、下次跟进时间。
     * 不允许通过此接口修改主负责人、经营状态等管控字段。
     *
     * @param customer 客户信息（须携带 version）
     * @return 更新后的客户对象
     */
    CrmCustomer edit(CrmCustomer customer);

    /**
     * 查询客户列表（按当前用户数据范围）
     *
     * @param query 查询条件
     * @return 客户列表
     */
    List<CrmCustomer> list(CrmCustomer query);

    /**
     * 查询客户列表（全部，仅管理员）
     *
     * @param query 查询条件
     * @return 客户列表
     */
    List<CrmCustomer> listAll(CrmCustomer query);
}
