package com.ruoyi.crm.dingtalk.service;

import com.ruoyi.crm.dingtalk.domain.DingTalkUserInfo;

/**
 * 钉钉身份交换服务接口
 * <p>
 * H5 免登：authCode → 钉钉用户 ID → CRM 身份映射 → 签发会话
 *
 * @author ruoyi-crm
 */
public interface DingTalkIdentityExchangeService
{
    /**
     * 用免登授权码换取钉钉用户信息
     * <p>
     * 授权码一次消费，不可重放。
     *
     * @param authCode 钉钉免登授权码
     * @return 钉钉用户信息
     */
    DingTalkUserInfo exchangeAuthCode(String authCode);

    /**
     * 根据钉钉用户 ID 查找已映射的系统用户 ID
     *
     * @param tenantId 租户 ID
     * @param dingtalkUserId 钉钉用户 ID
     * @return 系统用户 ID，未映射时返回 null
     */
    Long findSysUserId(String tenantId, String dingtalkUserId);

    /**
     * 判断钉钉用户是否已映射到系统用户
     *
     * @param tenantId 租户 ID
     * @param dingtalkUserId 钉钉用户 ID
     * @return true=已映射
     */
    boolean isIdentityMapped(String tenantId, String dingtalkUserId);

    /**
     * 创建或更新钉钉身份映射
     *
     * @param tenantId 租户 ID
     * @param dingtalkUserId 钉钉用户 ID
     * @param sysUserId 系统用户 ID
     * @param unionId 钉钉 UnionID（可选）
     */
    void mapIdentity(String tenantId, String dingtalkUserId, Long sysUserId, String unionId);
}
