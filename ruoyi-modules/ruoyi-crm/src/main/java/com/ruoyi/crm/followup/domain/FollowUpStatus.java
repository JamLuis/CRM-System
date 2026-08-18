package com.ruoyi.crm.followup.domain;

/**
 * 客户跟进状态枚举
 * <p>
 * 由系统自动计算，不允许用户手工修改。
 */
public enum FollowUpStatus
{
    NORMAL("正常"),
    INSUFFICIENT("不足"),
    SEVERE_INSUFFICIENT("严重不足"),
    NOT_ASSESSED("不考核");

    private final String label;

    FollowUpStatus(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public static FollowUpStatus fromString(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return NOT_ASSESSED;
        }
        for (FollowUpStatus s : values())
        {
            if (s.name().equalsIgnoreCase(value))
            {
                return s;
            }
        }
        return NOT_ASSESSED;
    }
}
