package com.ruoyi.crm.common.id;

/**
 * CRM 统一 ID 生成服务接口
 * <p>
 * 所有 CRM 实体在 INSERT 前通过此接口获取雪花 ID
 */
public interface IdGenerator {

    /**
     * 生成下一个全局唯一 ID
     *
     * @return 雪花 ID
     */
    long nextId();
}
