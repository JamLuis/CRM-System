package com.ruoyi.crm.customer.domain;

/**
 * 客户经营状态枚举
 * <p>
 * 正常、暂停跟进、已失效、已归档
 *
 * @author ruoyi-crm
 */
public enum OperatingStatus
{
    /** 正常 */
    NORMAL("正常"),
    /** 暂停跟进 */
    PAUSED("暂停跟进"),
    /** 已失效 */
    EXPIRED("已失效"),
    /** 已归档 */
    ARCHIVED("已归档");

    private final String value;

    OperatingStatus(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    /**
     * 从中文字符串解析枚举
     */
    public static OperatingStatus fromString(String text)
    {
        if (text == null || text.isEmpty())
        {
            return NORMAL;
        }
        for (OperatingStatus s : values())
        {
            if (s.value.equals(text) || s.name().equalsIgnoreCase(text))
            {
                return s;
            }
        }
        return NORMAL;
    }
}
