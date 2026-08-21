package com.ruoyi.crm.followup.domain;

/**
 * 跟进方式枚举
 */
public enum FollowUpMethod
{
    PHONE("电话"),
    WECHAT("微信"),
    IN_PERSON("面谈"),
    EMAIL("邮件"),
    OTHER("其他");

    private final String label;

    FollowUpMethod(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public static FollowUpMethod fromString(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new IllegalArgumentException("跟进方式不能为空");
        }
        for (FollowUpMethod m : values())
        {
            if (m.name().equalsIgnoreCase(value) || m.label.equals(value))
            {
                return m;
            }
        }
        throw new IllegalArgumentException("无效的跟进方式：" + value);
    }
}
