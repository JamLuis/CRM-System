package com.ruoyi.crm.followup.domain;

/**
 * 提醒计划状态枚举
 */
public enum ReminderPlanStatus
{
    ACTIVE("活动"),
    CANCELLED("已取消"),
    SUPERSEDED("已被替代");

    private final String label;

    ReminderPlanStatus(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public static ReminderPlanStatus fromString(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new IllegalArgumentException("提醒计划状态不能为空");
        }
        for (ReminderPlanStatus s : values())
        {
            if (s.name().equalsIgnoreCase(value))
            {
                return s;
            }
        }
        throw new IllegalArgumentException("无效的提醒计划状态：" + value);
    }
}
