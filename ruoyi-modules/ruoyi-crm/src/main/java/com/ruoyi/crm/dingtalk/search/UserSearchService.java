package com.ruoyi.crm.dingtalk.search;

import com.ruoyi.system.api.domain.SysUser;

import java.util.List;

/**
 * 人员搜索服务接口
 * <p>
 * 基于组织同步后的 sys_user 快照，提供按部门、姓名、手机号搜索人员的能力。
 * 管理员可触发单个用户刷新。
 *
 * @author ruoyi-crm
 */
public interface UserSearchService
{
    /**
     * 按关键词搜索人员（姓名/手机号/用户名模糊匹配）
     *
     * @param tenantId 租户 ID
     * @param keyword  关键词（姓名/手机号/用户名）
     * @return 匹配的用户列表
     */
    List<SysUser> search(String tenantId, String keyword);

    /**
     * 按部门搜索人员
     *
     * @param tenantId 租户 ID
     * @param deptId   部门 ID
     * @return 部门下的用户列表
     */
    List<SysUser> findByDept(String tenantId, Long deptId);

    /**
     * 按部门递归搜索人员（包含子部门）
     *
     * @param tenantId 租户 ID
     * @param deptId   部门 ID
     * @return 部门及子部门下的所有用户
     */
    List<SysUser> findByDeptRecursive(String tenantId, Long deptId);

    /**
     * 根据用户 ID 查询人员详情
     *
     * @param tenantId 租户 ID
     * @param userId   用户 ID
     * @return 用户信息
     */
    SysUser findById(String tenantId, Long userId);

    /**
     * 管理员手动刷新单个用户（从钉钉拉取最新信息）
     *
     * @param tenantId       租户 ID
     * @param dingtalkUserId 钉钉用户 ID
     * @return 刷新结果
     */
    boolean refreshUser(String tenantId, String dingtalkUserId);
}
