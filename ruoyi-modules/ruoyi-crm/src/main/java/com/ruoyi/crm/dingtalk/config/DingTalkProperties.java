package com.ruoyi.crm.dingtalk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 钉钉集成配置属性
 * <p>
 * 密钥（Client Secret）不在此配置中，仅从远端密钥服务/环境变量读取。
 *
 * @author ruoyi-crm
 */
@Component
@ConfigurationProperties(prefix = "crm.dingtalk")
public class DingTalkProperties
{
    /**
     * 是否启用钉钉集成
     */
    private boolean enabled = false;

    /**
     * 钉钉应用 Client ID（AppKey）
     */
    private String clientId;

    /**
     * 钉钉应用 Agent ID
     */
    private String agentId;

    /**
     * 企业 Corp ID
     */
    private String corpId;

    /**
     * Client Secret 的环境变量名（不直接存值）
     */
    private String clientSecretEnv = "DINGTALK_CLIENT_SECRET";

    /**
     * 钉钉 API 基础地址
     */
    private String apiBaseUrl = "https://oapi.dingtalk.com";

    /**
     * 获取 access_token 的接口路径
     */
    private String getTokenPath = "/gettoken";

    /**
     * 获取用户信息的接口路径
     */
    private String getUserInfoPath = "/topapi/v2/user/getuserinfo";

    /**
     * 获取部门列表的接口路径
     */
    private String getDeptListPath = "/topapi/v2/department/listsub";

    /**
     * 获取部门用户列表的接口路径
     */
    private String getDeptUserListPath = "/topapi/v2/user/list";

    /** 发送企业工作通知 */
    private String sendWorkNotificationPath = "/topapi/message/corpconversation/asyncsend_v2";

    /**
     * access_token 缓存时间（秒），默认 7000（钉钉 token 有效期 7200s）
     */
    private long tokenCacheSeconds = 7000;

    /** 钉钉根部门 ID。 */
    private Long rootDeptId = 1L;

    /** 钉钉根部门对应的 RuoYi 系统根部门 ID。 */
    private Long systemRootDeptId = 100L;

    // --- getters/setters ---

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public String getAgentId()
    {
        return agentId;
    }

    public void setAgentId(String agentId)
    {
        this.agentId = agentId;
    }

    public String getCorpId()
    {
        return corpId;
    }

    public void setCorpId(String corpId)
    {
        this.corpId = corpId;
    }

    public String getClientSecretEnv()
    {
        return clientSecretEnv;
    }

    public void setClientSecretEnv(String clientSecretEnv)
    {
        this.clientSecretEnv = clientSecretEnv;
    }

    public String getApiBaseUrl()
    {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl)
    {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getGetTokenPath()
    {
        return getTokenPath;
    }

    public void setGetTokenPath(String getTokenPath)
    {
        this.getTokenPath = getTokenPath;
    }

    public String getGetUserInfoPath()
    {
        return getUserInfoPath;
    }

    public void setGetUserInfoPath(String getUserInfoPath)
    {
        this.getUserInfoPath = getUserInfoPath;
    }

    public String getGetDeptListPath()
    {
        return getDeptListPath;
    }

    public void setGetDeptListPath(String getDeptListPath)
    {
        this.getDeptListPath = getDeptListPath;
    }

    public String getGetDeptUserListPath()
    {
        return getDeptUserListPath;
    }

    public void setGetDeptUserListPath(String getDeptUserListPath)
    {
        this.getDeptUserListPath = getDeptUserListPath;
    }

    public String getSendWorkNotificationPath()
    {
        return sendWorkNotificationPath;
    }

    public void setSendWorkNotificationPath(String sendWorkNotificationPath)
    {
        this.sendWorkNotificationPath = sendWorkNotificationPath;
    }

    public long getTokenCacheSeconds()
    {
        return tokenCacheSeconds;
    }

    public void setTokenCacheSeconds(long tokenCacheSeconds)
    {
        this.tokenCacheSeconds = tokenCacheSeconds;
    }

    public Long getRootDeptId()
    {
        return rootDeptId;
    }

    public void setRootDeptId(Long rootDeptId)
    {
        this.rootDeptId = rootDeptId;
    }

    public Long getSystemRootDeptId()
    {
        return systemRootDeptId;
    }

    public void setSystemRootDeptId(Long systemRootDeptId)
    {
        this.systemRootDeptId = systemRootDeptId;
    }

    /**
     * 从环境变量安全读取 Client Secret，不回显
     */
    public String getClientSecret()
    {
        return System.getenv(clientSecretEnv);
    }
}
