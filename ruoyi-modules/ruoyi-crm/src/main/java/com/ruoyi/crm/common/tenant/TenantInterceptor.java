package com.ruoyi.crm.common.tenant;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * 租户 MyBatis 拦截器
 * <p>
 * 拦截所有 INSERT 和 UPDATE 操作，自动为参数注入 tenant_id。
 * <p>
 * 对于 INSERT：如果参数对象有 tenantId 字段且为空，则从 {@link TenantContext} 注入。
 * 对于 UPDATE：在 WHERE 条件中追加 tenant_id 校验（通过参数注入）。
 * <p>
 * 注意：SELECT 查询的租户过滤通过 Mapper XML 中显式写 #{tenantId} 条件实现，
 * 此拦截器主要保证写入时 tenant_id 不被遗漏。
 *
 * @author ruoyi-crm
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class TenantInterceptor implements Interceptor
{
    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        if (parameter == null)
        {
            return invocation.proceed();
        }

        SqlCommandType sqlType = ms.getSqlCommandType();

        // 仅对 INSERT 和 UPDATE 注入 tenant_id
        if (sqlType == SqlCommandType.INSERT || sqlType == SqlCommandType.UPDATE)
        {
            injectTenantId(parameter);
        }

        return invocation.proceed();
    }

    /**
     * 为参数对象注入 tenant_id
     */
    private void injectTenantId(Object parameter)
    {
        String tenantId = TenantContext.getTenantId();

        // 处理 MyBatis 参数包装（@Param 注解会包装成 Map）
        Object target = parameter;
        if (parameter instanceof Map)
        {
            Map<?, ?> paramMap = (Map<?, ?>) parameter;
            // 尝试从 Map 中取出实体对象
            for (Object value : paramMap.values())
            {
                if (hasTenantIdField(value))
                {
                    target = value;
                    break;
                }
            }
        }

        if (hasTenantIdField(target))
        {
            try
            {
                java.lang.reflect.Field field = target.getClass().getDeclaredField("tenantId");
                field.setAccessible(true);
                if (field.get(target) == null || "".equals(field.get(target)))
                {
                    field.set(target, tenantId);
                    log.debug("注入 tenant_id: {}", tenantId);
                }
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                // 忽略没有 tenantId 字段的对象
            }
        }
    }

    /**
     * 检查对象是否有 tenantId 字段
     */
    private boolean hasTenantIdField(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        try
        {
            obj.getClass().getDeclaredField("tenantId");
            return true;
        }
        catch (NoSuchFieldException e)
        {
            return false;
        }
    }

    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties)
    {
    }
}
