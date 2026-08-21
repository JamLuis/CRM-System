package com.ruoyi.crm.datajob.domain;

/**
 * CRM 导入数据类型。
 */
public enum DataImportType
{
    /** 客户 */
    CUSTOMER,
    /** 联系人 */
    CONTACT,
    /** 跟进记录 */
    FOLLOW_UP;

    public static DataImportType fromString(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return CUSTOMER;
        }
        for (DataImportType type : values())
        {
            if (type.name().equalsIgnoreCase(value.trim()))
            {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的导入类型：" + value);
    }
}
