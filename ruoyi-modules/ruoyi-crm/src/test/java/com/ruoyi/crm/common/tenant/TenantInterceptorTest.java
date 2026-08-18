package com.ruoyi.crm.common.tenant;

import com.ruoyi.crm.common.domain.CrmBaseEntity;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TenantInterceptor 测试 — 验证 MyBatis 拦截器自动注入 tenant_id
 */
@DisplayName("TenantInterceptor 租户拦截测试")
class TenantInterceptorTest
{
    private TenantInterceptor interceptor;

    @BeforeEach
    void setup()
    {
        interceptor = new TenantInterceptor();
        interceptor.setProperties(new Properties());
        TenantContext.clear();
    }

    @AfterEach
    void cleanup()
    {
        TenantContext.clear();
    }

    @Test
    @DisplayName("INSERT 时 tenant_id 为空则自动从 TenantContext 注入")
    void testInjectTenantIdOnInsert() throws Exception
    {
        TenantContext.setTenantId("tenant-test-001");

        CrmBaseEntity entity = new CrmBaseEntity();
        assertNull(getField(entity, "tenantId"));

        // 直接调用 private injectTenantId 方法
        invokeInject(entity);

        assertEquals("tenant-test-001", getField(entity, "tenantId"));
    }

    @Test
    @DisplayName("INSERT 时 tenant_id 已有值则不覆盖")
    void testDoNotOverwriteExistingTenantId() throws Exception
    {
        TenantContext.setTenantId("tenant-context");

        CrmBaseEntity entity = new CrmBaseEntity();
        setField(entity, "tenantId", "existing-tenant");

        invokeInject(entity);

        assertEquals("existing-tenant", getField(entity, "tenantId"));
    }

    @Test
    @DisplayName("非 CrmBaseEntity 对象不报错")
    void testNonCrmBaseEntity()
    {
        TenantContext.setTenantId("tenant-x");
        Object obj = new Object();
        assertDoesNotThrow(() -> invokeInject(obj));
    }

    @Test
    @DisplayName("TenantContext 为 default 时注入 default")
    void testDefaultTenantInjection() throws Exception
    {
        // 不设置 tenant，使用默认值
        CrmBaseEntity entity = new CrmBaseEntity();
        invokeInject(entity);
        assertEquals("default", getField(entity, "tenantId"));
    }

    // --- helpers ---

    private Object getField(Object obj, String fieldName) throws Exception
    {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception
    {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private void invokeInject(Object target) throws Exception
    {
        Method m = TenantInterceptor.class.getDeclaredMethod("injectTenantId", Object.class);
        m.setAccessible(true);
        m.invoke(interceptor, target);
    }
}
