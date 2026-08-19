package com.ruoyi.crm.datajob.domain;

import java.io.Serializable;

/**
 * 导入行预检结果
 * <p>
 * 用于"上传—预检—确认"流程中逐行反馈校验结果。
 *
 * @author ruoyi-crm
 */
public class ImportRowResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** Excel 行号（从 1 开始，不含表头） */
    private Integer rowNum;
    /** 客户名称 */
    private String name;
    /** 是否通过预检 */
    private Boolean valid;
    /** 校验消息（失败原因，多条用分号分隔） */
    private String message;
    /** 确认执行后的结果：SUCCESS / FAILED / SKIPPED */
    private String result;
    /** 执行后创建的客户 ID（成功时） */
    private Long customerId;

    public ImportRowResult()
    {
    }

    public ImportRowResult(Integer rowNum, String name, Boolean valid, String message)
    {
        this.rowNum = rowNum;
        this.name = name;
        this.valid = valid;
        this.message = message;
    }

    public Integer getRowNum()
    {
        return rowNum;
    }

    public void setRowNum(Integer rowNum)
    {
        this.rowNum = rowNum;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Boolean getValid()
    {
        return valid;
    }

    public void setValid(Boolean valid)
    {
        this.valid = valid;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }
}
