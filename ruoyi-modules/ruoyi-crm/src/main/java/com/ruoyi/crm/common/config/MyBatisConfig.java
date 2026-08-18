package com.ruoyi.crm.common.config;

import com.ruoyi.crm.common.tenant.TenantInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置
 * <p>
 * 注册 CRM 专用拦截器。
 *
 * @author ruoyi-crm
 */
@Configuration
public class MyBatisConfig
{
    @Bean
    public TenantInterceptor tenantInterceptor()
    {
        return new TenantInterceptor();
    }
}
