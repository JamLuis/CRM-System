package com.ruoyi.crm.tenant.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CrmDingtalkDeptMapMapper
{
    Long selectSysDeptId(@Param("tenantId") String tenantId,
                         @Param("dingtalkDeptId") Long dingtalkDeptId);

    List<Map<String, Object>> selectAll(@Param("tenantId") String tenantId);

    int upsert(@Param("id") Long id,
               @Param("tenantId") String tenantId,
               @Param("dingtalkDeptId") Long dingtalkDeptId,
               @Param("sysDeptId") Long sysDeptId,
               @Param("deptName") String deptName);
}
