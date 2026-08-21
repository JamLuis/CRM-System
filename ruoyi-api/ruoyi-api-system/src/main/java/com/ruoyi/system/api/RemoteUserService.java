package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.factory.RemoteUserFallbackFactory;
import com.ruoyi.system.api.model.LoginUser;

import java.util.List;

/**
 * 用户服务
 * 
 * @author ruoyi
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteUserFallbackFactory.class)
public interface RemoteUserService
{
    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @param source 请求来源
     * @return 结果
     */
    @GetMapping("/user/info/{username}")
    public R<LoginUser> getUserInfo(@PathVariable("username") String username, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 注册用户信息
     *
     * @param sysUser 用户信息
     * @param source 请求来源
     * @return 结果
     */
    @PostMapping("/user/register")
    public R<Boolean> registerUserInfo(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：新增用户（钉钉组织同步用）
     *
     * @param sysUser 用户信息
     * @param source 请求来源
     * @return 用户 ID
     */
    @PostMapping("/user/inner/add")
    public R<Long> innerAddUser(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：修改用户（钉钉组织同步用）
     *
     * @param sysUser 用户信息
     * @param source 请求来源
     * @return 结果
     */
    @PutMapping("/user/inner/edit")
    public R<Boolean> innerEditUser(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：修改用户状态（钉钉离职同步用）
     *
     * @param userId 用户 ID
     * @param status 状态（0正常 1停用）
     * @param source 请求来源
     * @return 结果
     */
    @PutMapping("/user/inner/changeStatus")
    public R<Boolean> innerChangeUserStatus(@RequestParam("userId") Long userId,
                                            @RequestParam("status") String status,
                                            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：替换用户角色（CRM 显式授权用）
     */
    @PutMapping("/user/inner/authRole")
    public R<Boolean> innerAuthRoles(@RequestParam("userId") Long userId,
                                     @RequestBody Long[] roleIds,
                                     @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：按手机号查询用户（钉钉身份映射用）
     *
     * @param phonenumber 手机号
     * @param source 请求来源
     * @return 用户信息
     */
    @GetMapping("/user/inner/byPhone")
    public R<SysUser> innerGetUserByPhone(@RequestParam("phonenumber") String phonenumber,
                                           @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：按条件查询用户列表（CRM 人员搜索用）
     *
     * @param sysUser 查询条件（userName, phonenumber, deptId 等）
     * @param source  请求来源
     * @return 用户列表
     */
    @GetMapping("/user/inner/list")
    public R<List<SysUser>> innerList(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 内部调用：按用户 ID 查询用户详情
     *
     * @param userId 用户 ID
     * @param source 请求来源
     * @return 用户信息
     */
    @GetMapping("/user/inner/byId")
    public R<SysUser> innerGetUserById(@RequestParam("userId") Long userId,
                                       @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
