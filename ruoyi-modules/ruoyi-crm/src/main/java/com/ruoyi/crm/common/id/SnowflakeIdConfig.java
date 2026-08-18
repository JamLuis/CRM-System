package com.ruoyi.crm.common.id;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 雪花 ID 生成器配置
 * <p>
 * 通过 crm.snowflake.worker-id 和 crm.snowflake.datacenter-id 配置机器位。
 * 在多实例部署时，每个实例必须分配不同的 worker-id。
 *
 * @author ruoyi-crm
 */
@Configuration
public class SnowflakeIdConfig
{
    @Value("${crm.snowflake.worker-id:1}")
    private long workerId;

    @Value("${crm.snowflake.datacenter-id:1}")
    private long dataCenterId;

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator()
    {
        return new SnowflakeIdGenerator(workerId, dataCenterId);
    }

    /**
     * 将 SnowflakeIdGenerator 暴露为 IdGenerator 接口 Bean
     */
    @Bean
    public IdGenerator idGenerator(SnowflakeIdGenerator generator)
    {
        return generator::nextId;
    }
}
