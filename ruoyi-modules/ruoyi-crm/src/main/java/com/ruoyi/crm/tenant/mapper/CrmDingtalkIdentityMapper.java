package com.ruoyi.crm.tenant.mapper;

import com.ruoyi.crm.tenant.domain.CrmDingtalkIdentity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 钉钉身份映射 Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmDingtalkIdentityMapper
{
    /**
     * 根据钉钉用户 ID 查询身份映射
     */
    CrmDingtalkIdentity selectByDingtalkUserId(@Param("tenantId") String tenantId,
                                                @Param("dingtalkUserId") String dingtalkUserId);

    /**
     * 根据系统用户 ID 查询身份映射
     */
    CrmDingtalkIdentity selectBySysUserId(@Param("tenantId") String tenantId,
                                            @Param("sysUserId") Long sysUserId);

    /**
     * 新增
     */
    int insert(CrmDingtalkIdentity identity);

    /**
     * 更新
     */
    int update(CrmDingtalkIdentity identity);

    /**
     * 查询所有映射
     */
    List<CrmDingtalkIdentity> selectAll(@Param("tenantId") String tenantId);
}
