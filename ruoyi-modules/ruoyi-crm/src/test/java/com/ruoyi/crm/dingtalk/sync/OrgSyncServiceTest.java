package com.ruoyi.crm.dingtalk.sync;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 组织同步结果与游标信息测试
 */
@DisplayName("组织同步结果测试")
class OrgSyncServiceTest
{
    @Test
    @DisplayName("SyncResult 默认成功状态")
    void testSyncResultSuccess()
    {
        OrgSyncService.SyncResult result = new OrgSyncService.SyncResult(true);
        assertTrue(result.success);
        assertNull(result.error);
        assertEquals(0, result.deptCount);
        assertEquals(0, result.userCount);
        assertEquals(0, result.userUpdated);
        assertEquals(0, result.userDeactivated);
    }

    @Test
    @DisplayName("SyncResult 失败状态可设置错误信息")
    void testSyncResultFailure()
    {
        OrgSyncService.SyncResult result = new OrgSyncService.SyncResult(true);
        result.success = false;
        result.error = "Connection timeout";
        result.deptCount = 5;
        result.userCount = 10;
        result.userUpdated = 3;
        result.userDeactivated = 1;

        assertFalse(result.success);
        assertEquals("Connection timeout", result.error);
        assertEquals(5, result.deptCount);
        assertEquals(10, result.userCount);
        assertEquals(3, result.userUpdated);
        assertEquals(1, result.userDeactivated);
    }

    @Test
    @DisplayName("SyncCursorInfo 字段可正确设置和读取")
    void testSyncCursorInfo()
    {
        OrgSyncService.SyncCursorInfo info = new OrgSyncService.SyncCursorInfo();
        info.source = "DINGTALK";
        info.cursor = "1700000000000";
        info.lastSyncTime = new java.util.Date();
        info.status = "SUCCESS";

        assertEquals("DINGTALK", info.source);
        assertEquals("1700000000000", info.cursor);
        assertNotNull(info.lastSyncTime);
        assertEquals("SUCCESS", info.status);
    }
}
