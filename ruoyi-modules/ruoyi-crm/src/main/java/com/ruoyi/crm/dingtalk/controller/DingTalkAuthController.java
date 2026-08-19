package com.ruoyi.crm.dingtalk.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.dingtalk.config.DingTalkProperties;
import com.ruoyi.crm.dingtalk.domain.DingTalkUserInfo;
import com.ruoyi.crm.dingtalk.service.DingTalkIdentityExchangeService;
import com.ruoyi.crm.dingtalk.service.DingTalkLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉 H5 免登接口
 * <p>
 * H5 微应用通过 JSAPI 获取 authCode 后，调用此接口换取 CRM 会话。
 * 授权码一次消费，后端换取钉钉用户 ID 后查找身份映射。
 * <p>
 * 未映射身份返回待激活状态，不自动授予任何角色。
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/dingtalk")
public class DingTalkAuthController
{
    private static final Logger log = LoggerFactory.getLogger(DingTalkAuthController.class);

    @Autowired
    private DingTalkIdentityExchangeService identityExchangeService;

    @Autowired
    private DingTalkLoginService dingTalkLoginService;

    @Autowired
    private DingTalkProperties properties;

    /**
     * 钉钉 H5 免登登录（签发 CRM 会话）
     * <p>
     * 白名单接口（网关 ignore.whites 配置 /crm/v1/dingtalk/auth/login），无需 token。
     * authCode 一次消费，换取钉钉用户 ID 后查身份映射：
     * <ul>
     *   <li>已映射 → 校验用户状态，签发 JWT（status=MAPPED）</li>
     *   <li>未映射 → 返回 PENDING_ACTIVATION，不签发会话</li>
     * </ul>
     *
     * @param authCode 钉钉免登授权码
     * @return 登录结果（含 access_token）
     */
    @PostMapping("/auth/login")
    public R<Map<String, Object>> login(@RequestParam("authCode") String authCode)
    {
        if (!properties.isEnabled())
        {
            return R.fail("DingTalk integration is not enabled");
        }

        try
        {
            return R.ok(dingTalkLoginService.loginByAuthCode(authCode));
        }
        catch (Exception e)
        {
            log.error("DingTalk login failed", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * H5 免登 exchange
     * <p>
     * 前端通过钉钉 JSAPI requestAuthCode 获取 authCode 后 POST 到此接口。
     * 后端用 authCode 换取钉钉用户 ID，查找身份映射：
     * <ul>
     *   <li>已映射 → 返回系统用户 ID，前端走正常登录流程</li>
     *   <li>未映射 → 返回 PENDING_ACTIVATION，不签发会话</li>
     * </ul>
     *
     * @param authCode 钉钉免登授权码
     * @return 交换结果
     */
    @PostMapping("/auth/exchange")
    public R<Map<String, Object>> exchange(@RequestParam("authCode") String authCode)
    {
        if (!properties.isEnabled())
        {
            return R.fail("DingTalk integration is not enabled");
        }

        try
        {
            // 1. 授权码换取钉钉用户信息（一次消费）
            DingTalkUserInfo userInfo = identityExchangeService.exchangeAuthCode(authCode);

            if (userInfo.getUserid() == null)
            {
                return R.fail("Failed to get DingTalk user identity");
            }

            // 2. 查找身份映射（使用默认租户，实际应从 CorpId 解析）
            String tenantId = "default";
            Long sysUserId = identityExchangeService.findSysUserId(tenantId, userInfo.getUserid());

            Map<String, Object> result = new HashMap<>();
            result.put("dingtalkUserId", userInfo.getUserid());
            result.put("unionId", userInfo.getUnionid());

            if (sysUserId != null)
            {
                // 已映射 — 返回系统用户 ID，前端走正常登录
                result.put("sysUserId", sysUserId);
                result.put("status", "MAPPED");
                log.debug("DingTalk auth exchange success: dingtalkUserId={}, sysUserId={}",
                        userInfo.getUserid(), sysUserId);
                return R.ok(result);
            }
            else
            {
                // 未映射 — 待激活，不签发会话
                result.put("status", "PENDING_ACTIVATION");
                log.debug("DingTalk auth exchange: identity not mapped, dingtalkUserId={}",
                        userInfo.getUserid());
                return R.ok(result);
            }
        }
        catch (Exception e)
        {
            log.error("DingTalk auth exchange failed", e);
            return R.fail("DingTalk auth exchange failed");
        }
    }

    /**
     * 获取钉钉微应用配置（供前端 JSAPI 鉴权）
     * <p>
     * 返回 corpId 和 agentId，前端用于 dd.config 鉴权。
     * 不返回 clientSecret。
     */
    @PostMapping("/config")
    public R<Map<String, Object>> getConfig()
    {
        if (!properties.isEnabled())
        {
            return R.fail("DingTalk integration is not enabled");
        }

        Map<String, Object> config = new HashMap<>();
        config.put("corpId", properties.getCorpId());
        config.put("agentId", properties.getAgentId());
        return R.ok(config);
    }
}
