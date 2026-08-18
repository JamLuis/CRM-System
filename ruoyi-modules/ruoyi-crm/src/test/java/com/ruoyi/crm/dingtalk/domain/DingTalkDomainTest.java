package com.ruoyi.crm.dingtalk.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 钉钉领域对象测试
 */
@DisplayName("钉钉领域对象测试")
class DingTalkDomainTest
{
    @Test
    @DisplayName("DingTalkUserInfo 字段设置和读取")
    void testDingTalkUserInfo()
    {
        DingTalkUserInfo info = new DingTalkUserInfo();
        info.setUserid("user123");
        info.setUnionid("union456");
        info.setDeviceId("device789");
        info.setSysLevel(true);

        assertEquals("user123", info.getUserid());
        assertEquals("union456", info.getUnionid());
        assertEquals("device789", info.getDeviceId());
        assertTrue(info.getSysLevel());
    }

    @Test
    @DisplayName("DingTalkDeptUser 字段设置和读取")
    void testDingTalkDeptUser()
    {
        DingTalkDeptUser user = new DingTalkDeptUser();
        user.setUserid("dt001");
        user.setName("张三");
        user.setMobile("13800138000");
        user.setTitle("销售经理");
        user.setDeptIdList(Arrays.asList(1L, 2L, 3L));
        user.setActive(true);
        user.setUnionid("union001");
        user.setEmail("zhangsan@example.com");

        assertEquals("dt001", user.getUserid());
        assertEquals("张三", user.getName());
        assertEquals("13800138000", user.getMobile());
        assertEquals("销售经理", user.getTitle());
        assertEquals(3, user.getDeptIdList().size());
        assertTrue(user.getActive());
        assertEquals("union001", user.getUnionid());
        assertEquals("zhangsan@example.com", user.getEmail());
    }

    @Test
    @DisplayName("DingTalkDept 字段设置和读取")
    void testDingTalkDept()
    {
        DingTalkDept dept = new DingTalkDept();
        dept.setDeptId(100L);
        dept.setParentId(1L);
        dept.setName("销售部");
        dept.setOrder(5L);
        dept.setCreateTime(1700000000000L);
        dept.setContainSub(true);

        assertEquals(100L, dept.getDeptId());
        assertEquals(1L, dept.getParentId());
        assertEquals("销售部", dept.getName());
        assertEquals(5L, dept.getOrder());
        assertEquals(1700000000000L, dept.getCreateTime());
        assertTrue(dept.getContainSub());
    }

    @Test
    @DisplayName("DingTalkDeptUser active 为 false 时表示离职")
    void testDingTalkDeptUserInactive()
    {
        DingTalkDeptUser user = new DingTalkDeptUser();
        user.setActive(false);
        assertFalse(user.getActive());
    }
}
