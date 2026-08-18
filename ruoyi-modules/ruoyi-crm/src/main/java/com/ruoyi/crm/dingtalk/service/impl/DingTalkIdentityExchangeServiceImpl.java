package com.ruoyi.crm.dingtalk.service.impl;

import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.dingtalk.client.DingTalkClient;
import com.ruoyi.crm.dingtalk.config.DingTalkProperties;
import com.ruoyi.crm.dingtalk.domain.DingTalkUserInfo;
import com.ruoyi.crm.dingtalk.service.DingTalkIdentityExchangeService;
import com.ruoyi.crm.tenant.domain.CrmDingtalkIdentity;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkIdentityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 钉钉身份交换服务实现
 * <p>
 * 流程：H5 authCode → DingTalkClient.getUserInfoByAuthCode → 钉钉用户 ID
 * → CrmDingtalkIdentityMapper 查找映射 → 返回系统用户 ID
 * <p>
 * 安全约束：
 * <ul>
 *   <li>授权码一次消费（钉钉侧保证）</li>
 *   <li>未映射身份不自动授予角色，仅返回待激活状态</li>
 *   <li>不将 authCode 或 token 写入日志</li>
 * </ul>
 *
 * @author ruoyi-crm
 */
@Service
public class DingTalkIdentityExchangeServiceImpl implements DingTalkIdentityExchangeService
{
    private static final Logger log = LoggerFactory.getLogger(DingTalkIdentityExchangeServiceImpl.class);

    @Autowired
    private DingTalkClient dingTalkClient;

    @Autowired
    private DingTalkProperties properties;

    @Autowired
    private CrmDingtalkIdentityMapper identityMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    public DingTalkUserInfo exchangeAuthCode(String authCode)
    {
        if (authCode == null || authCode.isEmpty())
        {
            throw new IllegalArgumentException("authCode must not be null or empty");
        }
        log.debug("Exchanging DingTalk authCode for user info");
        return dingTalkClient.getUserInfoByAuthCode(authCode);
    }

    @Override
    public Long findSysUserId(String tenantId, String dingtalkUserId)
    {
        CrmDingtalkIdentity identity = identityMapper.selectByDingtalkUserId(tenantId, dingtalkUserId);
        return identity != null ? identity.getSysUserId() : null;
    }

    @Override
    public boolean isIdentityMapped(String tenantId, String dingtalkUserId)
    {
        return findSysUserId(tenantId, dingtalkUserId) != null;
    }

    @Override
    public void mapIdentity(String tenantId, String dingtalkUserId, Long sysUserId, String unionId)
    {
        CrmDingtalkIdentity existing = identityMapper.selectByDingtalkUserId(tenantId, dingtalkUserId);
        if (existing != null)
        {
            // 更新映射
            existing.setSysUserId(sysUserId);
            existing.setUnionId(unionId);
            identityMapper.update(existing);
            log.debug("Updated DingTalk identity mapping: dingtalkUserId={}, sysUserId={}",
                    dingtalkUserId, sysUserId);
        }
        else
        {
            // 新建映射
            CrmDingtalkIdentity identity = new CrmDingtalkIdentity();
            identity.setId(idGenerator.nextId());
            identity.setTenantId(tenantId);
            identity.setDingtalkUserId(dingtalkUserId);
            identity.setSysUserId(sysUserId);
            identity.setUnionId(unionId);
            identityMapper.insert(identity);
            log.debug("Created DingTalk identity mapping: dingtalkUserId={}, sysUserId={}",
                    dingtalkUserId, sysUserId);
        }
    }
}
