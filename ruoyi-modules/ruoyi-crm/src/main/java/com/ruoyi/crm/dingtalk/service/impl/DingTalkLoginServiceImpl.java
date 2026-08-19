package com.ruoyi.crm.dingtalk.service.impl;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.UserStatus;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.dingtalk.domain.DingTalkUserInfo;
import com.ruoyi.crm.dingtalk.service.DingTalkIdentityExchangeService;
import com.ruoyi.crm.dingtalk.service.DingTalkLoginService;
import com.ruoyi.system.api.RemoteLogService;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysLogininfor;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉 H5 免登登录服务实现
 * <p>
 * 流程：authCode → 钉钉用户信息 → 身份映射 → 系统用户校验 → TokenService 签发 JWT。
 * 与 RuoYi 密码登录保持一致的会话语义（同一 TokenService、同一登录日志）。
 *
 * @author ruoyi-crm
 */
@Service
public class DingTalkLoginServiceImpl implements DingTalkLoginService
{
    private static final Logger log = LoggerFactory.getLogger(DingTalkLoginServiceImpl.class);

    @Autowired
    private DingTalkIdentityExchangeService identityExchangeService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteLogService remoteLogService;

    @Autowired
    private TokenService tokenService;

    @Override
    public Map<String, Object> loginByAuthCode(String authCode)
    {
        if (StringUtils.isBlank(authCode))
        {
            throw new ServiceException("免登授权码不能为空");
        }

        // 1. 授权码换取钉钉用户信息（一次消费）
        DingTalkUserInfo userInfo = identityExchangeService.exchangeAuthCode(authCode);
        if (userInfo == null || StringUtils.isBlank(userInfo.getUserid()))
        {
            throw new ServiceException("钉钉免登授权码无效");
        }

        // 2. 查找身份映射（当前单租户，使用默认租户）
        String tenantId = TenantContext.getTenantId();
        Long sysUserId = identityExchangeService.findSysUserId(tenantId, userInfo.getUserid());

        Map<String, Object> result = new HashMap<>();
        result.put("dingtalkUserId", userInfo.getUserid());
        result.put("unionId", userInfo.getUnionid());

        if (sysUserId == null)
        {
            // 未映射 — 待激活，不签发会话
            result.put("status", "PENDING_ACTIVATION");
            log.info("DingTalk login: identity not mapped, dingtalkUserId={}", userInfo.getUserid());
            return result;
        }

        // 3. 查询系统用户（内部调用）
        R<SysUser> userResult = remoteUserService.innerGetUserById(sysUserId, SecurityConstants.INNER);
        if (userResult == null || userResult.getCode() != R.SUCCESS || userResult.getData() == null)
        {
            recordLogininfor(null, Constants.LOGIN_FAIL, "钉钉免登失败：映射用户不存在");
            throw new ServiceException("钉钉免登失败：映射的系统用户不存在");
        }
        SysUser sysUser = userResult.getData();

        // 4. 用户状态校验
        if (UserStatus.DELETED.getCode().equals(sysUser.getDelFlag()))
        {
            recordLogininfor(sysUser.getUserName(), Constants.LOGIN_FAIL, "钉钉免登失败：账号已删除");
            throw new ServiceException("钉钉免登失败：账号已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(sysUser.getStatus()))
        {
            recordLogininfor(sysUser.getUserName(), Constants.LOGIN_FAIL, "钉钉免登失败：账号已停用");
            throw new ServiceException("钉钉免登失败：账号已停用，请联系管理员");
        }

        // 5. 获取完整登录用户（角色与权限）
        R<LoginUser> loginResult = remoteUserService.getUserInfo(sysUser.getUserName(), SecurityConstants.INNER);
        if (loginResult == null || loginResult.getCode() != R.SUCCESS || loginResult.getData() == null)
        {
            recordLogininfor(sysUser.getUserName(), Constants.LOGIN_FAIL, "钉钉免登失败：获取用户信息失败");
            throw new ServiceException("钉钉免登失败：获取用户信息失败");
        }

        // 6. 签发会话（与密码登录同一 TokenService）
        LoginUser loginUser = loginResult.getData();
        Map<String, Object> tokenMap = tokenService.createToken(loginUser);

        result.put("status", "MAPPED");
        result.put("sysUserId", sysUserId);
        result.put("access_token", tokenMap.get("access_token"));
        result.put("expires_in", tokenMap.get("expires_in"));

        recordLogininfor(sysUser.getUserName(), Constants.LOGIN_SUCCESS, "钉钉免登成功");
        log.info("DingTalk login success: dingtalkUserId={}, sysUserId={}, username={}",
                userInfo.getUserid(), sysUserId, sysUser.getUserName());
        return result;
    }

    /**
     * 记录登录日志（与 ruoyi-auth 登录日志同表）
     */
    private void recordLogininfor(String username, String status, String message)
    {
        try
        {
            SysLogininfor logininfor = new SysLogininfor();
            logininfor.setUserName(username);
            logininfor.setIpaddr(IpUtils.getIpAddr());
            logininfor.setMsg(message);
            if (Constants.LOGIN_SUCCESS.equals(status))
            {
                logininfor.setStatus(Constants.LOGIN_SUCCESS_STATUS);
            }
            else
            {
                logininfor.setStatus(Constants.LOGIN_FAIL_STATUS);
            }
            logininfor.setAccessTime(new Date());
            remoteLogService.saveLogininfor(logininfor, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            log.warn("Failed to record DingTalk login log: {}", message, e);
        }
    }
}
