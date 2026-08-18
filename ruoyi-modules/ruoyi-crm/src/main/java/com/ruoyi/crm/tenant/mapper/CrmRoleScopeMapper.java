package com.ruoyi.crm.tenant.mapper;

import com.ruoyi.crm.tenant.domain.CrmRoleScope;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据范围 Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmRoleScopeMapper
{
    /**
     * 根据角色 ID 查询数据范围
     */
    CrmRoleScope selectByRoleId(@Param("tenantId") String tenantId,
                                  @Param("roleId") Long roleId);

    /**
     * 查询所有角色范围
     */
    List<CrmRoleScope> selectAll(@Param("tenantId") String tenantId);

    /**
     * 新增
     */
    int insert(CrmRoleScope scope);

    /**
     * 更新
     */
    int update(CrmRoleScope scope);

    /**
     * 删除
     */
    int deleteById(@Param("tenantId") String tenantId, @Param("id") Long id);
}
