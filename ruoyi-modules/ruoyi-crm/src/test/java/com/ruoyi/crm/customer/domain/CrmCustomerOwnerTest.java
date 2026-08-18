package com.ruoyi.crm.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrmCustomerOwner 实体测试
 */
@DisplayName("CrmCustomerOwner 实体测试")
class CrmCustomerOwnerTest
{
    @Test
    @DisplayName("CrmCustomerOwner 正确设置和获取所有字段")
    void testAllFields()
    {
        CrmCustomerOwner owner = new CrmCustomerOwner();
        owner.setId(5001L);
        owner.setCustomerId(1001L);
        owner.setUserId(200L);
        owner.setUserName("张三");
        owner.setRoleType(OwnerRoleType.PRIMARY.name());
        owner.setStatus("ACTIVE");
        owner.setVersion(0);
        owner.setDelFlag("0");

        assertEquals(5001L, owner.getId());
        assertEquals(1001L, owner.getCustomerId());
        assertEquals(200L, owner.getUserId());
        assertEquals("张三", owner.getUserName());
        assertEquals("PRIMARY", owner.getRoleType());
        assertEquals("ACTIVE", owner.getStatus());
        assertEquals(Integer.valueOf(0), owner.getVersion());
        assertEquals("0", owner.getDelFlag());
    }
}
