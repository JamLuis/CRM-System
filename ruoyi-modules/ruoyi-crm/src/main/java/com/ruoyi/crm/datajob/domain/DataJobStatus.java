package com.ruoyi.crm.datajob.domain;

/**
 * 数据作业状态
 * <p>
 * 导入流转：VALIDATED（预检完成待确认）→ RUNNING → SUCCESS / FAILED<br>
 * 导出流转：PENDING → RUNNING → SUCCESS（文件就绪）/ FAILED；SUCCESS 过期后 → EXPIRED
 *
 * @author ruoyi-crm
 */
public enum DataJobStatus
{
    /** 已提交待执行（导出） */
    PENDING,
    /** 执行中 */
    RUNNING,
    /** 导入预检完成，等待确认执行 */
    VALIDATED,
    /** 成功 */
    SUCCESS,
    /** 失败 */
    FAILED,
    /** 导出文件已过期 */
    EXPIRED;

    public static DataJobStatus fromString(String value)
    {
        for (DataJobStatus s : values())
        {
            if (s.name().equalsIgnoreCase(value))
            {
                return s;
            }
        }
        throw new IllegalArgumentException("未知的作业状态：" + value);
    }
}
