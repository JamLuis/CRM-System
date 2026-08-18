package com.ruoyi.crm.dingtalk.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.dingtalk.search.UserSearchService;
import com.ruoyi.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 人员搜索接口
 * <p>
 * 基于组织同步后的 sys_user 快照，提供按部门、姓名、手机号搜索人员的能力。
 * 管理员可触发单个用户刷新。
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/users")
public class UserSearchController
{
    @Autowired
    private UserSearchService userSearchService;

    /**
     * 按关键词搜索人员（姓名/手机号/用户名模糊匹配）
     *
     * @param keyword 关键词
     * @return 匹配的用户列表
     */
    @GetMapping("/search")
    public R<List<SysUser>> search(@RequestParam(value = "keyword", required = false) String keyword)
    {
        String tenantId = TenantContext.getTenantId();
        List<SysUser> users = userSearchService.search(tenantId, keyword);
        return R.ok(users);
    }

    /**
     * 按部门搜索人员
     *
     * @param deptId 部门 ID
     * @return 部门下的用户列表
     */
    @GetMapping("/dept")
    public R<List<SysUser>> findByDept(@RequestParam("deptId") Long deptId)
    {
        String tenantId = TenantContext.getTenantId();
        List<SysUser> users = userSearchService.findByDept(tenantId, deptId);
        return R.ok(users);
    }

    /**
     * 按部门递归搜索人员（包含子部门）
     *
     * @param deptId 部门 ID
     * @return 部门及子部门下的所有用户
     */
    @GetMapping("/dept/recursive")
    public R<List<SysUser>> findByDeptRecursive(@RequestParam("deptId") Long deptId)
    {
        String tenantId = TenantContext.getTenantId();
        List<SysUser> users = userSearchService.findByDeptRecursive(tenantId, deptId);
        return R.ok(users);
    }

    /**
     * 根据用户 ID 查询人员详情
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @GetMapping("/info")
    public R<SysUser> findById(@RequestParam("userId") Long userId)
    {
        String tenantId = TenantContext.getTenantId();
        SysUser user = userSearchService.findById(tenantId, userId);
        if (user != null)
        {
            return R.ok(user);
        }
        return R.fail("用户不存在");
    }

    /**
     * 管理员手动刷新单个用户（从钉钉拉取最新信息）
     *
     * @param dingtalkUserId 钉钉用户 ID
     * @return 刷新结果
     */
    @GetMapping("/refresh")
    public R<Boolean> refreshUser(@RequestParam("dingtalkUserId") String dingtalkUserId)
    {
        String tenantId = TenantContext.getTenantId();
        boolean success = userSearchService.refreshUser(tenantId, dingtalkUserId);
        return R.ok(success);
    }
}
