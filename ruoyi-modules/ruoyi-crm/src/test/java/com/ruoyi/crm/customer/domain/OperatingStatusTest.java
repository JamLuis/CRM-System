package com.ruoyi.crm.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 经营状态枚举测试
 */
@DisplayName("经营状态枚举测试")
class OperatingStatusTest
{
    @Test
    @DisplayName("fromString 正确解析中文值")
    void testFromStringChinese()
    {
        assertEquals(OperatingStatus.NORMAL, OperatingStatus.fromString("正常"));
        assertEquals(OperatingStatus.PAUSED, OperatingStatus.fromString("暂停跟进"));
        assertEquals(OperatingStatus.EXPIRED, OperatingStatus.fromString("已失效"));
        assertEquals(OperatingStatus.ARCHIVED, OperatingStatus.fromString("已归档"));
    }

    @Test
    @DisplayName("fromString 正确解析英文名")
    void testFromStringEnglish()
    {
        assertEquals(OperatingStatus.NORMAL, OperatingStatus.fromString("NORMAL"));
        assertEquals(OperatingStatus.PAUSED, OperatingStatus.fromString("PAUSED"));
        assertEquals(OperatingStatus.EXPIRED, OperatingStatus.fromString("EXPIRED"));
        assertEquals(OperatingStatus.ARCHIVED, OperatingStatus.fromString("ARCHIVED"));
    }

    @Test
    @DisplayName("fromString 空值或 null 返回 NORMAL")
    void testFromStringNull()
    {
        assertEquals(OperatingStatus.NORMAL, OperatingStatus.fromString(null));
        assertEquals(OperatingStatus.NORMAL, OperatingStatus.fromString(""));
    }

    @Test
    @DisplayName("fromString 无法匹配时返回 NORMAL")
    void testFromStringUnknown()
    {
        assertEquals(OperatingStatus.NORMAL, OperatingStatus.fromString("未知状态"));
    }

    @Test
    @DisplayName("getValue 返回中文值")
    void testGetValue()
    {
        assertEquals("正常", OperatingStatus.NORMAL.getValue());
        assertEquals("暂停跟进", OperatingStatus.PAUSED.getValue());
        assertEquals("已失效", OperatingStatus.EXPIRED.getValue());
        assertEquals("已归档", OperatingStatus.ARCHIVED.getValue());
    }
}
