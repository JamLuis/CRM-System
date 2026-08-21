package com.ruoyi.crm.tenant.mapper;

import com.ruoyi.crm.tenant.domain.CrmDingtalkDirectoryUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.ruoyi.system.api.domain.SysRole;

public interface CrmDingtalkDirectoryUserMapper
{
    CrmDingtalkDirectoryUser selectByDingtalkUserId(@Param("tenantId") String tenantId,
                                                     @Param("dingtalkUserId") String dingtalkUserId);

    List<CrmDingtalkDirectoryUser> selectAuthorizationList(@Param("tenantId") String tenantId,
                                                            @Param("keyword") String keyword,
                                                            @Param("accessStatus") String accessStatus);

    int upsert(CrmDingtalkDirectoryUser user);

    List<SysRole> selectAssignableRoles();

    int countGrantedBySysUserId(@Param("tenantId") String tenantId,
                                @Param("sysUserId") Long sysUserId);
}
