package com.ruoyi.crm.customer.service.impl;

import com.ruoyi.crm.customer.domain.CrmContact;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.permission.ScopeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("联系人敏感信息展示策略测试")
class ContactSensitiveVisibilityTest
{
    private final ContactServiceImpl service = new ContactServiceImpl();

    @Test
    @DisplayName("系统管理员和 CRM 全量管理员可查看明文")
    void testAdministratorsCanViewPlaintext()
    {
        CrmCustomer customer = customer(20L, "21");

        assertTrue(service.isSensitiveVisible(customer, 30L, true, ScopeType.DEPT));
        assertTrue(service.isSensitiveVisible(customer, 30L, false, ScopeType.ALL));
    }

    @Test
    @DisplayName("主负责人和协同处理人可查看明文")
    void testCustomerMembersCanViewPlaintext()
    {
        CrmCustomer customer = customer(20L, " 21,22 ");

        assertTrue(service.isSensitiveVisible(customer, 20L, false, ScopeType.DEPT));
        assertTrue(service.isSensitiveVisible(customer, 21L, false, ScopeType.DEPT));
    }

    @Test
    @DisplayName("非客户成员不能查看明文")
    void testNonMemberCannotViewPlaintext()
    {
        CrmCustomer customer = customer(20L, "21,22");

        assertFalse(service.isSensitiveVisible(customer, 30L, false, ScopeType.DEPT));
        assertFalse(service.isSensitiveVisible(customer, null, false, ScopeType.ALL));
    }

    @Test
    @DisplayName("非客户成员响应清除明文并生成脱敏值")
    void testHiddenResponseContainsMaskedValuesOnly()
    {
        CrmContact contact = new CrmContact();
        contact.setPhoneNumber("13800138000");
        contact.setEmail("contact@example.com");
        contact.setWechatId("contact_wechat");

        service.applySensitiveVisibility(contact, false);

        assertNull(contact.getPhoneNumber());
        assertNull(contact.getEmail());
        assertNull(contact.getWechatId());
        assertEquals("138****8000", contact.getPhoneMasked());
        assertEquals("c***@example.com", contact.getEmailMasked());
        assertEquals("co***at", contact.getWechatMasked());
    }

    private CrmCustomer customer(Long primaryOwnerId, String collaboratorIds)
    {
        CrmCustomer customer = new CrmCustomer();
        customer.setPrimaryOwnerId(primaryOwnerId);
        customer.setCollaboratorIds(collaboratorIds);
        return customer;
    }
}
