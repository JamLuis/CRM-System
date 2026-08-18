package com.ruoyi.crm.common.tenant;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * CRM 租户上下文
 * <p>
 * 使用 {@link TransmittableThreadLocal} 在线程间传递租户 ID。
 * 由 {@code TenantContextInterceptor} 在请求入口处设置。
 *
 * @author ruoyi-crm
 */
public class TenantContext
{
    private static final TransmittableThreadLocal<String> TENANT_HOLDER = new TransmittableThreadLocal<>();

    /** 默认租户 ID */
    public static final String DEFAULT_TENANT_ID = "default";

    /**
     * 设置当前租户 ID
     */
    public static void setTenantId(String tenantId)
    {
        TENANT_HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户 ID
     *
     * @return 租户 ID，未设置时返回 {@link #DEFAULT_TENANT_ID}
     */
    public static String getTenantId()
    {
        String tenantId = TENANT_HOLDER.get();
        return tenantId == null ? DEFAULT_TENANT_ID : tenantId;
    }

    /**
     * 清除当前线程的租户上下文
     */
    public static void clear()
    {
        TENANT_HOLDER.remove();
    }
}
