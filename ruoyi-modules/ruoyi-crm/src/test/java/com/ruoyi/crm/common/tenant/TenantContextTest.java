package com.ruoyi.crm.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 租户上下文测试
 */
@DisplayName("租户上下文测试")
class TenantContextTest
{
    @AfterEach
    void cleanup()
    {
        TenantContext.clear();
    }

    @Test
    @DisplayName("默认租户为 default")
    void testDefaultTenant()
    {
        assertEquals("default", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("设置和获取租户 ID")
    void testSetGet()
    {
        TenantContext.setTenantId("tenant-001");
        assertEquals("tenant-001", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("清除后恢复默认租户")
    void testClear()
    {
        TenantContext.setTenantId("tenant-002");
        TenantContext.clear();
        assertEquals("default", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("线程间租户隔离")
    void testThreadIsolation() throws InterruptedException
    {
        TenantContext.setTenantId("main-tenant");
        Thread t = new Thread(() ->
        {
            TenantContext.setTenantId("other-tenant");
            assertEquals("other-tenant", TenantContext.getTenantId());
        });
        t.start();
        t.join();
        assertEquals("main-tenant", TenantContext.getTenantId());
    }
}
