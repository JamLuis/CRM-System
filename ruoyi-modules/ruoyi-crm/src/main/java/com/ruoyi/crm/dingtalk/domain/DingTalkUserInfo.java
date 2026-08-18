package com.ruoyi.crm.dingtalk.domain;

import java.io.Serializable;

/**
 * 钉钉用户信息（免登 exchange 返回）
 *
 * @author ruoyi-crm
 */
public class DingTalkUserInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 钉钉用户 ID（企业内唯一） */
    private String userid;

    /** 钉钉 UnionID */
    private String unionid;

    /** 设备 ID（可选） */
    private String deviceId;

    /** 是否为管理员 */
    private Boolean sysLevel;

    public String getUserid()
    {
        return userid;
    }

    public void setUserid(String userid)
    {
        this.userid = userid;
    }

    public String getUnionid()
    {
        return unionid;
    }

    public void setUnionid(String unionid)
    {
        this.unionid = unionid;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    public Boolean getSysLevel()
    {
        return sysLevel;
    }

    public void setSysLevel(Boolean sysLevel)
    {
        this.sysLevel = sysLevel;
    }
}
