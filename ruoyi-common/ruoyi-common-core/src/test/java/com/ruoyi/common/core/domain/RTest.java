package com.ruoyi.common.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * R 响应体单元测试
 */
class RTest
{
    @Test
    void testOkNoData()
    {
        R<Void> r = R.ok();
        assertEquals(R.SUCCESS, r.getCode());
        assertNull(r.getData());
        assertNull(r.getMsg());
    }

    @Test
    void testOkWithData()
    {
        R<String> r = R.ok("hello");
        assertEquals(R.SUCCESS, r.getCode());
        assertEquals("hello", r.getData());
    }

    @Test
    void testFail()
    {
        R<Void> r = R.fail();
        assertEquals(R.FAIL, r.getCode());
    }

    @Test
    void testFailWithMsg()
    {
        R<Void> r = R.fail("error message");
        assertEquals(R.FAIL, r.getCode());
        assertEquals("error message", r.getMsg());
    }
}
