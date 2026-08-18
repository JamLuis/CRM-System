package com.ruoyi.crm.audit.mapper;

import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 审计事件 Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmAuditEventMapper
{
    /**
     * 新增审计事件
     */
    int insert(CrmAuditEvent event);

    /**
     * 根据实体查询审计事件
     */
    List<CrmAuditEvent> selectByEntity(@Param("tenantId") String tenantId,
                                       @Param("entityType") String entityType,
                                       @Param("entityId") String entityId);

    /**
     * 根据操作人查询
     */
    List<CrmAuditEvent> selectByOperator(@Param("tenantId") String tenantId,
                                          @Param("operatorId") Long operatorId);

    /**
     * 按时间范围查询
     */
    List<CrmAuditEvent> selectByTimeRange(@Param("tenantId") String tenantId,
                                           @Param("startTime") Date startTime,
                                           @Param("endTime") Date endTime);
}
