package com.ruoyi.crm.tenant.mapper;

import com.ruoyi.crm.tenant.domain.CrmTenant;
import java.util.List;

/**
 * 租户 Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmTenantMapper
{
    /**
     * 根据租户 ID 查询
     */
    CrmTenant selectByTenantId(String tenantId);

    /**
     * 查询所有租户
     */
    List<CrmTenant> selectAll();

    /**
     * 新增租户
     */
    int insert(CrmTenant tenant);

    /**
     * 更新租户
     */
    int update(CrmTenant tenant);
}
