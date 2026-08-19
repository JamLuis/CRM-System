package com.ruoyi.crm.datajob.mapper;

import com.ruoyi.crm.datajob.domain.CrmDataJob;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * CRM 数据作业 Mapper
 *
 * @author ruoyi-crm
 */
public interface CrmDataJobMapper
{
    /**
     * 按作业 ID 查询
     */
    CrmDataJob selectByJobId(@Param("tenantId") String tenantId, @Param("jobId") Long jobId);

    /**
     * 查询作业列表（按类型过滤，可选）
     */
    List<CrmDataJob> selectList(@Param("tenantId") String tenantId, @Param("jobType") String jobType);

    /**
     * 插入作业
     */
    int insert(CrmDataJob job);

    /**
     * 更新作业
     */
    int update(CrmDataJob job);

    /**
     * 查询已过期但仍为 SUCCESS 的导出作业（全局扫描，用于定时过期标记）
     */
    List<CrmDataJob> selectExpiredExports(@Param("now") Date now);
}
