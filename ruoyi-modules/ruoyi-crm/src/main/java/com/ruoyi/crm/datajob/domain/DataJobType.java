package com.ruoyi.crm.datajob.domain;

/**
 * 数据作业类型
 *
 * @author ruoyi-crm
 */
public enum DataJobType
{
    /** 导入 */
    IMPORT,
    /** 导出 */
    EXPORT;

    public static DataJobType fromString(String value)
    {
        for (DataJobType t : values())
        {
            if (t.name().equalsIgnoreCase(value))
            {
                return t;
            }
        }
        throw new IllegalArgumentException("未知的作业类型：" + value);
    }
}
