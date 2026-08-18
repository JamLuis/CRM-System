package com.ruoyi.crm.tenant.mapper;

import com.ruoyi.crm.tenant.domain.CrmOrgSyncCursor;
import org.apache.ibatis.annotations.Param;

/**
 * 组织架构同步游标 Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmOrgSyncCursorMapper
{
    /**
     * 根据数据源查询游标
     */
    CrmOrgSyncCursor selectBySource(@Param("tenantId") String tenantId,
                                     @Param("source") String source);

    /**
     * 新增
     */
    int insert(CrmOrgSyncCursor cursor);

    /**
     * 更新
     */
    int update(CrmOrgSyncCursor cursor);
}
