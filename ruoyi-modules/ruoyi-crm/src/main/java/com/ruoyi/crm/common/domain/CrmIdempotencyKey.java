package com.ruoyi.crm.common.domain;

import java.util.Date;

/**
 * 幂等键对象 crm_idempotency_key
 * <p>
 * 用于接口幂等性控制，防止重复提交。
 *
 * @author ruoyi-crm
 */
public class CrmIdempotencyKey implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;
    /** 租户 ID */
    private String tenantId;
    /** 幂等键 */
    private String idempotencyKey;
    /** 请求体哈希 */
    private String requestHash;
    /** 响应状态码 */
    private Integer responseStatus;
    /** 响应体缓存 */
    private String responseBody;
    /** 过期时间 */
    private Date expireTime;
    /** 创建时间 */
    private Date createTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getIdempotencyKey()
    {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey)
    {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestHash()
    {
        return requestHash;
    }

    public void setRequestHash(String requestHash)
    {
        this.requestHash = requestHash;
    }

    public Integer getResponseStatus()
    {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus)
    {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody()
    {
        return responseBody;
    }

    public void setResponseBody(String responseBody)
    {
        this.responseBody = responseBody;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
