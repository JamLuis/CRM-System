package com.ruoyi.crm.followup.domain;

/**
 * 附件所属业务类型枚举
 */
public enum AttachmentOwnerType
{
    FOLLOW_UP("跟进记录"),
    CUSTOMER("客户");

    private final String label;

    AttachmentOwnerType(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public static AttachmentOwnerType fromString(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new IllegalArgumentException("附件所属类型不能为空");
        }
        for (AttachmentOwnerType t : values())
        {
            if (t.name().equalsIgnoreCase(value))
            {
                return t;
            }
        }
        throw new IllegalArgumentException("无效的附件所属类型：" + value);
    }
}
