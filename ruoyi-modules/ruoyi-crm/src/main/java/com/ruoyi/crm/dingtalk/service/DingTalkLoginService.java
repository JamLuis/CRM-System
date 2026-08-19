package com.ruoyi.crm.dingtalk.service;

import java.util.Map;

/**
 * 钉钉 H5 免登登录服务接口
 * <p>
 * authCode → 钉钉用户 ID → CRM 身份映射 → 校验系统用户 → 签发 CRM 会话（JWT）。
 * 未映射身份不签发会话，返回待激活状态。
 *
 * @author ruoyi-crm
 */
public interface DingTalkLoginService
{
    /**
     * 钉钉免登登录
     *
     * @param authCode 钉钉 JSAPI 免登授权码（一次消费）
     * @return 登录结果：
     *         已映射 → status=MAPPED + access_token/expires_in/sysUserId/dingtalkUserId；
     *         未映射 → status=PENDING_ACTIVATION
     */
    Map<String, Object> loginByAuthCode(String authCode);
}
