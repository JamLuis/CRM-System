package com.ruoyi.crm.datajob.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.crm.common.domain.CrmBaseEntity;

import java.util.Date;

/**
 * CRM 数据作业实体 crm_data_job
 * <p>
 * 统一承载导入与导出作业：
 * <ul>
 *   <li>导入：上传—预检（VALIDATED）—确认执行（RUNNING→SUCCESS/FAILED），逐行结果存 row_results</li>
 *   <li>导出：异步生成（PENDING→RUNNING→SUCCESS），文件在 expire_time 后过期（EXPIRED）</li>
 * </ul>
 *
 * @author ruoyi-crm
 */
public class CrmDataJob extends CrmBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 作业ID（雪花） */
    private Long jobId;
    /** 作业类型（IMPORT/EXPORT） */
    private String jobType;
    /** 作业状态（PENDING/RUNNING/VALIDATED/SUCCESS/FAILED/EXPIRED） */
    private String status;
    /** 源文件名（导入）/ 导出文件名 */
    private String fileName;
    /** 文件存储键（导入源文件或导出产物） */
    private String storageKey;
    /** 导出查询条件（JSON，继承页面数据范围） */
    private String queryCondition;
    /** 总行数 */
    private Integer totalCount;
    /** 成功行数 */
    private Integer successCount;
    /** 失败行数 */
    private Integer failedCount;
    /** 逐行结果（JSON 数组） */
    private String rowResults;
    /** 导出文件过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
    /** 操作人 ID */
    private Long operatorId;
    /** 操作人名称 */
    private String operatorName;
    /** 错误信息 */
    private String errorMsg;
    /** 开始执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    public Long getJobId()
    {
        return jobId;
    }

    public void setJobId(Long jobId)
    {
        this.jobId = jobId;
    }

    public String getJobType()
    {
        return jobType;
    }

    public void setJobType(String jobType)
    {
        this.jobType = jobType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getStorageKey()
    {
        return storageKey;
    }

    public void setStorageKey(String storageKey)
    {
        this.storageKey = storageKey;
    }

    public String getQueryCondition()
    {
        return queryCondition;
    }

    public void setQueryCondition(String queryCondition)
    {
        this.queryCondition = queryCondition;
    }

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(Integer successCount)
    {
        this.successCount = successCount;
    }

    public Integer getFailedCount()
    {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount)
    {
        this.failedCount = failedCount;
    }

    public String getRowResults()
    {
        return rowResults;
    }

    public void setRowResults(String rowResults)
    {
        this.rowResults = rowResults;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getFinishTime()
    {
        return finishTime;
    }

    public void setFinishTime(Date finishTime)
    {
        this.finishTime = finishTime;
    }
}
