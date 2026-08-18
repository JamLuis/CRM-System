package com.ruoyi.crm.followup.domain;

/**
 * 附件状态枚举
 */
public enum AttachmentStatus
{
    PENDING_SCAN("待扫描"),
    AVAILABLE("可用"),
    QUARANTINED("隔离"),
    DELETED("已删除");

    private final String label;

    AttachmentStatus(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public static AttachmentStatus fromString(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new IllegalArgumentException("附件状态不能为空");
        }
        for (AttachmentStatus s : values())
        {
            if (s.name().equalsIgnoreCase(value))
            {
                return s;
            }
        }
        throw new IllegalArgumentException("无效的附件状态：" + value);
    }
}
