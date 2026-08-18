package com.ruoyi.crm.tenant.domain;

import com.ruoyi.crm.common.domain.CrmBaseEntity;

/**
 * 钉钉身份映射对象 crm_dingtalk_identity
 *
 * @author ruoyi-crm
 */
public class CrmDingtalkIdentity extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;
    /** 钉钉用户 ID */
    private String dingtalkUserId;
    /** 系统用户 ID */
    private Long sysUserId;
    /** 钉钉 UnionID */
    private String unionId;

    // 注意：tenantId 继承自 CrmBaseEntity

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getDingtalkUserId()
    {
        return dingtalkUserId;
    }

    public void setDingtalkUserId(String dingtalkUserId)
    {
        this.dingtalkUserId = dingtalkUserId;
    }

    public Long getSysUserId()
    {
        return sysUserId;
    }

    public void setSysUserId(Long sysUserId)
    {
        this.sysUserId = sysUserId;
    }

    public String getUnionId()
    {
        return unionId;
    }

    public void setUnionId(String unionId)
    {
        this.unionId = unionId;
    }
}
