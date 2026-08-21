package com.ruoyi.crm.customer.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmContact;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.mapper.CrmContactMapper;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.permission.CustomerAccessGuard;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.crm.permission.ScopeType;
import com.ruoyi.system.api.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 联系人服务测试
 */
@DisplayName("联系人服务测试")
class ContactServiceImplTest
{
    private CrmContactMapper contactMapper;
    private CrmCustomerMapper customerMapper;
    private IdGenerator idGenerator;
    private PermissionService permissionService;
    private CustomerAccessGuard customerAccessGuard;
    private AuditEventService auditEventService;
    private CustomerTimelineService timelineService;
    private ContactServiceImpl contactService;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() throws Exception
    {
        contactMapper = Mockito.mock(CrmContactMapper.class);
        customerMapper = Mockito.mock(CrmCustomerMapper.class);
        idGenerator = Mockito.mock(IdGenerator.class);
        permissionService = Mockito.mock(PermissionService.class);
        customerAccessGuard = Mockito.mock(CustomerAccessGuard.class);
        auditEventService = Mockito.mock(AuditEventService.class);
        timelineService = Mockito.mock(CustomerTimelineService.class);

        when(idGenerator.nextId()).thenReturn(3001L);
        doNothing().when(permissionService).check(any(PermissionContext.class));

        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(1L);
        loginUser.setUsername("admin");
        securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
        securityUtilsMock.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);

        contactService = new ContactServiceImpl();

        setField(contactService, "contactMapper", contactMapper);
        setField(contactService, "customerMapper", customerMapper);
        setField(contactService, "idGenerator", idGenerator);
        setField(contactService, "permissionService", permissionService);
        setField(contactService, "customerAccessGuard", customerAccessGuard);
        setField(contactService, "auditEventService", auditEventService);
        setField(contactService, "timelineService", timelineService);
    }

    @AfterEach
    void tearDown()
    {
        securityUtilsMock.close();
        TenantContext.clear();
    }

    @Test
    @DisplayName("创建联系人 - 手机号规范化正确执行")
    void testCreateContactPhoneNormalization()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        when(contactMapper.selectByCustomerAndPhone("test-tenant", 1001L, "8613800138000"))
                .thenReturn(null);
        when(contactMapper.insert(any(CrmContact.class))).thenReturn(1);

        CrmContact contact = new CrmContact();
        contact.setCustomerId(1001L);
        contact.setName("李四");
        contact.setCountryCode("86");
        contact.setPhoneNumber("138 0013-8000");

        CrmContact result = contactService.create(contact);

        assertEquals("8613800138000", result.getPhoneNumber());
        assertEquals("861****8000", result.getPhoneMasked());
        assertEquals("有效", result.getStatus());
        assertEquals(Integer.valueOf(0), result.getVersion());

        verify(contactMapper).insert(any(CrmContact.class));
    }

    @Test
    @DisplayName("创建联系人 - 同客户手机号重复时抛出异常")
    void testCreateContactDuplicatePhone()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmContact existing = new CrmContact();
        existing.setContactId(3000L);
        when(contactMapper.selectByCustomerAndPhone("test-tenant", 1001L, "8613800138000"))
                .thenReturn(existing);

        CrmContact contact = new CrmContact();
        contact.setCustomerId(1001L);
        contact.setCountryCode("86");
        contact.setPhoneNumber("13800138000");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> contactService.create(contact));
        assertTrue(ex.getMessage().contains("相同手机号"));
    }

    @Test
    @DisplayName("创建联系人 - 邮箱脱敏正确执行")
    void testCreateContactEmailMasking()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(contactMapper.insert(any(CrmContact.class))).thenReturn(1);

        CrmContact contact = new CrmContact();
        contact.setCustomerId(1001L);
        contact.setName("王五");
        contact.setEmail("wangwu@example.com");

        CrmContact result = contactService.create(contact);

        assertEquals("w***@example.com", result.getEmailMasked());
    }

    @Test
    @DisplayName("创建联系人 - 微信号脱敏正确执行")
    void testCreateContactWechatMasking()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(contactMapper.insert(any(CrmContact.class))).thenReturn(1);

        CrmContact contact = new CrmContact();
        contact.setCustomerId(1001L);
        contact.setName("赵六");
        contact.setWechatId("zhaoliu_wx");

        CrmContact result = contactService.create(contact);

        assertEquals("zh***wx", result.getWechatMasked());
    }

    @Test
    @DisplayName("停用联系人 - 乐观锁冲突时抛出异常")
    void testDeactivateOptimisticLockConflict()
    {
        TenantContext.setTenantId("test-tenant");

        CrmContact existing = new CrmContact();
        existing.setContactId(3001L);
        existing.setCustomerId(1001L);
        existing.setVersion(0);

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);

        when(contactMapper.selectByContactId("test-tenant", 3001L)).thenReturn(existing);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(contactMapper.deactivate("test-tenant", 3001L, "admin", 0)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> contactService.deactivate(3001L));
    }

    @Test
    @DisplayName("查询联系人详情 - 联系人不存在时抛出异常")
    void testDetailNotFound()
    {
        TenantContext.setTenantId("test-tenant");
        when(contactMapper.selectByContactId("test-tenant", 9999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> contactService.detail(9999L));
    }

    @Test
    @DisplayName("查询联系人列表 - 客户负责人直接看到明文")
    void testListPrimaryOwnerCanViewSensitiveFields()
    {
        TenantContext.setTenantId("test-tenant");
        stubOperator(20L, false);

        CrmCustomer customer = customer(1001L, 20L, "21");
        CrmContact contact = sensitiveContact(3001L, 1001L);
        when(customerAccessGuard.check(1001L, com.ruoyi.crm.permission.PermissionCode.CRM_CONTACT_READ))
                .thenReturn(customer);
        when(contactMapper.selectByCustomer("test-tenant", 1001L)).thenReturn(Arrays.asList(contact));

        CrmContact result = contactService.listByCustomer(1001L).get(0);

        assertEquals("13800138000", result.getPhoneNumber());
        assertEquals("contact@example.com", result.getEmail());
        assertEquals("contact_wechat", result.getWechatId());
    }

    @Test
    @DisplayName("查询联系人详情 - 客户协同处理人直接看到明文")
    void testDetailCollaboratorCanViewSensitiveFields()
    {
        TenantContext.setTenantId("test-tenant");
        stubOperator(21L, false);

        CrmCustomer customer = customer(1001L, 20L, " 21,22 ");
        CrmContact contact = sensitiveContact(3001L, 1001L);
        when(contactMapper.selectByContactId("test-tenant", 3001L)).thenReturn(contact);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmContact result = contactService.detail(3001L);

        assertEquals("13800138000", result.getPhoneNumber());
        assertEquals("contact@example.com", result.getEmail());
        assertEquals("contact_wechat", result.getWechatId());
    }

    @Test
    @DisplayName("查询联系人详情 - CRM 全量管理员直接看到明文")
    void testDetailCrmAdminCanViewSensitiveFields()
    {
        TenantContext.setTenantId("test-tenant");
        stubOperator(30L, false);

        CrmCustomer customer = customer(1001L, 20L, "21");
        CrmContact contact = sensitiveContact(3001L, 1001L);
        when(contactMapper.selectByContactId("test-tenant", 3001L)).thenReturn(contact);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(permissionService.getScopeType("test-tenant", 30L)).thenReturn(ScopeType.ALL);

        CrmContact result = contactService.detail(3001L);

        assertEquals("13800138000", result.getPhoneNumber());
        assertEquals("contact@example.com", result.getEmail());
        assertEquals("contact_wechat", result.getWechatId());
    }

    @Test
    @DisplayName("查询联系人详情 - 非客户成员只返回脱敏信息")
    void testDetailNonMemberCannotViewSensitiveFields()
    {
        TenantContext.setTenantId("test-tenant");
        stubOperator(30L, false);

        CrmCustomer customer = customer(1001L, 20L, "21");
        CrmContact contact = sensitiveContact(3001L, 1001L);
        contact.setPhoneMasked(null);
        contact.setEmailMasked(null);
        contact.setWechatMasked(null);
        when(contactMapper.selectByContactId("test-tenant", 3001L)).thenReturn(contact);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(permissionService.getScopeType("test-tenant", 30L)).thenReturn(ScopeType.DEPT);

        CrmContact result = contactService.detail(3001L);

        assertNull(result.getPhoneNumber());
        assertNull(result.getEmail());
        assertNull(result.getWechatId());
        assertEquals("138****8000", result.getPhoneMasked());
        assertEquals("c***@example.com", result.getEmailMasked());
        assertEquals("co***at", result.getWechatMasked());
    }

    private void stubOperator(Long userId, boolean admin)
    {
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(userId);
        securityUtilsMock.when(() -> SecurityUtils.isAdmin(userId)).thenReturn(admin);
    }

    private CrmCustomer customer(Long customerId, Long primaryOwnerId, String collaboratorIds)
    {
        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(customerId);
        customer.setPrimaryOwnerId(primaryOwnerId);
        customer.setCollaboratorIds(collaboratorIds);
        customer.setOperatingStatus("正常");
        return customer;
    }

    private CrmContact sensitiveContact(Long contactId, Long customerId)
    {
        CrmContact contact = new CrmContact();
        contact.setContactId(contactId);
        contact.setCustomerId(customerId);
        contact.setPhoneNumber("13800138000");
        contact.setPhoneMasked("138****8000");
        contact.setEmail("contact@example.com");
        contact.setEmailMasked("c***@example.com");
        contact.setWechatId("contact_wechat");
        contact.setWechatMasked("co***at");
        return contact;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception
    {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
