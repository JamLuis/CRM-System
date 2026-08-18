package com.ruoyi.crm.customer.domain;

/**
 * 客户生命周期阶段枚举
 * <p>
 * 新获取、待跟进、初步意向、商机客户、成交客户
 *
 * @author ruoyi-crm
 */
public enum LifecycleStage
{
    /** 新获取 */
    NEW("新获取"),
    /** 待跟进 */
    PENDING("待跟进"),
    /** 初步意向 */
    INITIAL_INTENT("初步意向"),
    /** 商机客户 */
    OPPORTUNITY("商机客户"),
    /** 成交客户 */
    CLOSED_WON("成交客户");

    private final String value;

    LifecycleStage(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public static LifecycleStage fromString(String text)
    {
        if (text == null || text.isEmpty())
        {
            return NEW;
        }
        for (LifecycleStage s : values())
        {
            if (s.value.equals(text) || s.name().equalsIgnoreCase(text))
            {
                return s;
            }
        }
        return NEW;
    }
}
