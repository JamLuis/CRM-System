package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;

import java.util.List;

/**
 * 用户服务降级处理
 * 
 * @author ruoyi
 */
@Component
public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteUserFallbackFactory.class);

    @Override
    public RemoteUserService create(Throwable throwable)
    {
        log.error("用户服务调用失败:{}", throwable.getMessage());
        return new RemoteUserService()
        {
            @Override
            public R<LoginUser> getUserInfo(String username, String source)
            {
                return R.fail("获取用户失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> registerUserInfo(SysUser sysUser, String source)
            {
                return R.fail("注册用户失败:" + throwable.getMessage());
            }

            @Override
            public R<Long> innerAddUser(SysUser sysUser, String source)
            {
                return R.fail("内部新增用户失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> innerEditUser(SysUser sysUser, String source)
            {
                return R.fail("内部修改用户失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> innerChangeUserStatus(Long userId, String status, String source)
            {
                return R.fail("内部修改用户状态失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> innerAuthRoles(Long userId, Long[] roleIds, String source)
            {
                return R.fail("内部授权用户角色失败:" + throwable.getMessage());
            }

            @Override
            public R<SysUser> innerGetUserByPhone(String phonenumber, String source)
            {
                return R.fail("内部按手机号查询用户失败:" + throwable.getMessage());
            }

            @Override
            public R<List<SysUser>> innerList(SysUser sysUser, String source)
            {
                return R.fail("内部查询用户列表失败:" + throwable.getMessage());
            }

            @Override
            public R<SysUser> innerGetUserById(Long userId, String source)
            {
                return R.fail("内部按 ID 查询用户失败:" + throwable.getMessage());
            }
        };
    }
}
