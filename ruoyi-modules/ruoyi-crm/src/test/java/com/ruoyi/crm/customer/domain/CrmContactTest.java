package com.ruoyi.crm.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrmContact 实体测试
 */
@DisplayName("CrmContact 实体测试")
class CrmContactTest
{
    @Test
    @DisplayName("CrmContact 正确设置和获取所有字段")
    void testAllFields()
    {
        CrmContact contact = new CrmContact();
        contact.setContactId(3001L);
        contact.setCustomerId(1001L);
        contact.setName("李四");
        contact.setPhoneType("手机");
        contact.setCountryCode("86");
        contact.setPhoneNumber("8613800138000");
        contact.setPhoneMasked("138****8000");
        contact.setEmail("lisi@example.com");
        contact.setEmailMasked("l***@example.com");
        contact.setWechatId("lisi_wx");
        contact.setWechatMasked("li***_x");
        contact.setResponsibility("采购");
        contact.setTitle("经理");
        contact.setIsDecisionMaker(true);
        contact.setRemark("关键决策人");
        contact.setStatus("有效");
        contact.setVersion(0);
        contact.setDelFlag("0");

        assertEquals(3001L, contact.getContactId());
        assertEquals(1001L, contact.getCustomerId());
        assertEquals("李四", contact.getName());
        assertEquals("手机", contact.getPhoneType());
        assertEquals("86", contact.getCountryCode());
        assertEquals("8613800138000", contact.getPhoneNumber());
        assertEquals("138****8000", contact.getPhoneMasked());
        assertEquals("lisi@example.com", contact.getEmail());
        assertEquals("l***@example.com", contact.getEmailMasked());
        assertEquals("lisi_wx", contact.getWechatId());
        assertEquals("li***_x", contact.getWechatMasked());
        assertEquals("采购", contact.getResponsibility());
        assertEquals("经理", contact.getTitle());
        assertTrue(contact.getIsDecisionMaker());
        assertEquals("关键决策人", contact.getRemark());
        assertEquals("有效", contact.getStatus());
        assertEquals(Integer.valueOf(0), contact.getVersion());
        assertEquals("0", contact.getDelFlag());
    }
}
