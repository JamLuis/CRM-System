package com.ruoyi.crm.permission.mapper;

import org.apache.ibatis.annotations.Param;

public interface CrmDataScopeMapper
{
    int countDeptOrDescendant(@Param("operatorDeptId") Long operatorDeptId,
                              @Param("targetDeptId") Long targetDeptId);
}
