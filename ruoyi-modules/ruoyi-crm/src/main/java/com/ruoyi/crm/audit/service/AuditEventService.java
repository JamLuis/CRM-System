package com.ruoyi.crm.audit.service;

import com.ruoyi.crm.audit.domain.CrmAuditEvent;

import java.util.List;

/**
 * CRM 审计事件服务接口
 */
public interface AuditEventService
{
    /**
     * 记录审计事件（在调用方事务内执行）
     *
     * @param event 审计事件（id 由服务层赋值）
     */
    void record(CrmAuditEvent event);

    /**
     * 按实体查询审计事件
     */
    List<CrmAuditEvent> findByEntity(String tenantId, String entityType, String entityId);

    /**
     * 按操作人查询审计事件
     */
    List<CrmAuditEvent> findByOperator(String tenantId, Long operatorId);

    /**
     * 按时间范围查询审计事件
     */
    List<CrmAuditEvent> findByTimeRange(String tenantId, java.util.Date startTime, java.util.Date endTime);
}
