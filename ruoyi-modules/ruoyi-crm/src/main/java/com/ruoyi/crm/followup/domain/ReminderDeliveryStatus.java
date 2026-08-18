package com.ruoyi.crm.followup.domain;

/**
 * 提醒投递状态枚举
 * <p>
 * 状态流转：
 * <ul>
 *   <li>PENDING → SENT → COMPLETED</li>
 *   <li>PENDING → CANCELLED</li>
 *   <li>PENDING → RETRYING → SENT / FAILED</li>
 * </ul>
 */
public enum ReminderDeliveryStatus
{
    PENDING("待调度"),
    RETRYING("重试中"),
    SENT("已发送"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    FAILED("最终失败");

    private final String label;

    ReminderDeliveryStatus(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    /**
     * 判断状态是否为"活跃"（可继续调度或重试）
     */
    public boolean isActive()
    {
        return this == PENDING || this == RETRYING;
    }

    public static ReminderDeliveryStatus fromString(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new IllegalArgumentException("提醒投递状态不能为空");
        }
        for (ReminderDeliveryStatus s : values())
        {
            if (s.name().equalsIgnoreCase(value))
            {
                return s;
            }
        }
        throw new IllegalArgumentException("无效的提醒投递状态：" + value);
    }
}
