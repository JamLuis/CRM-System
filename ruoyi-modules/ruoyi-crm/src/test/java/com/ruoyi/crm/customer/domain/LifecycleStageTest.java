package com.ruoyi.crm.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 生命周期阶段枚举测试
 */
@DisplayName("生命周期阶段枚举测试")
class LifecycleStageTest
{
    @Test
    @DisplayName("fromString 正确解析中文值")
    void testFromStringChinese()
    {
        assertEquals(LifecycleStage.NEW, LifecycleStage.fromString("新获取"));
        assertEquals(LifecycleStage.PENDING, LifecycleStage.fromString("待跟进"));
        assertEquals(LifecycleStage.INITIAL_INTENT, LifecycleStage.fromString("初步意向"));
        assertEquals(LifecycleStage.OPPORTUNITY, LifecycleStage.fromString("商机客户"));
        assertEquals(LifecycleStage.CLOSED_WON, LifecycleStage.fromString("成交客户"));
    }

    @Test
    @DisplayName("fromString 正确解析英文名")
    void testFromStringEnglish()
    {
        assertEquals(LifecycleStage.NEW, LifecycleStage.fromString("NEW"));
        assertEquals(LifecycleStage.PENDING, LifecycleStage.fromString("PENDING"));
    }

    @Test
    @DisplayName("fromString 空值或 null 返回 NEW")
    void testFromStringNull()
    {
        assertEquals(LifecycleStage.NEW, LifecycleStage.fromString(null));
        assertEquals(LifecycleStage.NEW, LifecycleStage.fromString(""));
    }

    @Test
    @DisplayName("getValue 返回中文值")
    void testGetValue()
    {
        assertEquals("新获取", LifecycleStage.NEW.getValue());
        assertEquals("待跟进", LifecycleStage.PENDING.getValue());
        assertEquals("成交客户", LifecycleStage.CLOSED_WON.getValue());
    }
}
