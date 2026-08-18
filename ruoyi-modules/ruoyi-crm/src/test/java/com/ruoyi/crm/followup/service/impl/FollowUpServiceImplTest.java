package com.ruoyi.crm.followup.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.followup.domain.CrmAttachment;
import com.ruoyi.crm.followup.domain.CrmFollowUp;
import com.ruoyi.crm.followup.mapper.CrmAttachmentMapper;
import com.ruoyi.crm.followup.mapper.CrmFollowUpContactMapper;
import com.ruoyi.crm.followup.mapper.CrmFollowUpMapper;
import com.ruoyi.crm.followup.service.ReminderService;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.system.api.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 跟进记录服务测试
 */
@DisplayName("跟进记录服务测试")
class FollowUpServiceImplTest
{
    private CrmFollowUpMapper followUpMapper;
    private CrmFollowUpContactMapper followUpContactMapper;
    private CrmAttachmentMapper attachmentMapper;
    private CrmCustomerMapper customerMapper;
    private IdGenerator idGenerator;
    private PermissionService permissionService;
    private AuditEventService auditEventService;
    private CustomerTimelineService timelineService;
    private ReminderService reminderService;
    private FollowUpServiceImpl followUpService;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() throws Exception
    {
        followUpMapper = Mockito.mock(CrmFollowUpMapper.class);
        followUpContactMapper = Mockito.mock(CrmFollowUpContactMapper.class);
        attachmentMapper = Mockito.mock(CrmAttachmentMapper.class);
        customerMapper = Mockito.mock(CrmCustomerMapper.class);
        idGenerator = Mockito.mock(IdGenerator.class);
        permissionService = Mockito.mock(PermissionService.class);
        auditEventService = Mockito.mock(AuditEventService.class);
        timelineService = Mockito.mock(CustomerTimelineService.class);
        reminderService = Mockito.mock(ReminderService.class);

        when(idGenerator.nextId()).thenReturn(6001L);
        doNothing().when(permissionService).check(any(PermissionContext.class));

        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(1L);
        loginUser.setUsername("admin");
        securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
        securityUtilsMock.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);

        followUpService = new FollowUpServiceImpl();
        setField(followUpService, "followUpMapper", followUpMapper);
        setField(followUpService, "followUpContactMapper", followUpContactMapper);
        setField(followUpService, "attachmentMapper", attachmentMapper);
        setField(followUpService, "customerMapper", customerMapper);
        setField(followUpService, "idGenerator", idGenerator);
        setField(followUpService, "permissionService", permissionService);
        setField(followUpService, "auditEventService", auditEventService);
        setField(followUpService, "timelineService", timelineService);
        setField(followUpService, "reminderService", reminderService);
    }

    @AfterEach
    void tearDown()
    {
        securityUtilsMock.close();
        TenantContext.clear();
    }

    @Test
    @DisplayName("创建跟进 - 面谈方式无附件时成功")
    void testCreateInPersonFollowUpSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setVersion(0);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(followUpMapper.insert(any(CrmFollowUp.class))).thenReturn(1);
        when(followUpMapper.selectByFollowUpId("test-tenant", 6001L)).thenReturn(new CrmFollowUp());

        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setCustomerId(1001L);
        followUp.setMethod("面谈");
        followUp.setFollowUpAt(new Date());
        followUp.setContent("客户拜访");

        CrmFollowUp result = followUpService.create(followUp, null, null);

        assertNotNull(result);
        verify(followUpMapper).insert(any(CrmFollowUp.class));
        verify(auditEventService).record(any());
        verify(timelineService).record(any());
    }

    @Test
    @DisplayName("创建跟进 - 客户不存在时抛出异常")
    void testCreateCustomerNotFound()
    {
        TenantContext.setTenantId("test-tenant");
        when(customerMapper.selectByCustomerId("test-tenant", 9999L)).thenReturn(null);

        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setCustomerId(9999L);
        followUp.setMethod("面谈");
        followUp.setFollowUpAt(new Date());

        assertThrows(IllegalArgumentException.class, () -> followUpService.create(followUp, null, null));
    }

    @Test
    @DisplayName("创建跟进 - 未来时间抛出异常")
    void testCreateFutureFollowUpAtThrows()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setCustomerId(1001L);
        followUp.setMethod("面谈");

        Date future = new Date(System.currentTimeMillis() + 86400000L * 2);
        followUp.setFollowUpAt(future);

        assertThrows(IllegalArgumentException.class, () -> followUpService.create(followUp, null, null));
    }

    @Test
    @DisplayName("创建跟进 - 电话方式无图片附件时抛出异常")
    void testCreatePhoneWithoutImageThrows()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setCustomerId(1001L);
        followUp.setMethod("电话");
        followUp.setFollowUpAt(new Date());

        assertThrows(IllegalArgumentException.class, () -> followUpService.create(followUp, null, null));
    }

    @Test
    @DisplayName("创建跟进 - 电话方式有可用图片附件时成功")
    void testCreatePhoneWithImageSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setVersion(0);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmAttachment imageAttachment = new CrmAttachment();
        imageAttachment.setAttachmentId(5001L);
        imageAttachment.setStatus("AVAILABLE");
        imageAttachment.setContentType("image/jpeg");
        when(attachmentMapper.selectByAttachmentId("test-tenant", 5001L)).thenReturn(imageAttachment);

        when(followUpMapper.insert(any(CrmFollowUp.class))).thenReturn(1);
        when(followUpMapper.selectByFollowUpId("test-tenant", 6001L)).thenReturn(new CrmFollowUp());

        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setCustomerId(1001L);
        followUp.setMethod("电话");
        followUp.setFollowUpAt(new Date());
        followUp.setContent("电话沟通");

        List<Long> attachmentIds = Arrays.asList(5001L);

        CrmFollowUp result = followUpService.create(followUp, null, attachmentIds);

        assertNotNull(result);
        verify(followUpMapper).insert(any(CrmFollowUp.class));
    }

    @Test
    @DisplayName("作废跟进 - 已作废记录抛出异常")
    void testVoidAlreadyVoidedThrows()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUp existing = new CrmFollowUp();
        existing.setFollowUpId(6001L);
        existing.setCustomerId(1001L);
        existing.setIsVoided(true);

        when(followUpMapper.selectByFollowUpId("test-tenant", 6001L)).thenReturn(existing);

        assertThrows(IllegalStateException.class, () -> followUpService.void_(6001L, "误录"));
    }

    @Test
    @DisplayName("作废跟进 - 成功")
    void testVoidSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUp existing = new CrmFollowUp();
        existing.setFollowUpId(6001L);
        existing.setCustomerId(1001L);
        existing.setIsVoided(false);

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);

        when(followUpMapper.selectByFollowUpId("test-tenant", 6001L)).thenReturn(existing);
        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(followUpMapper.markVoided("test-tenant", 6001L, "误录", "admin")).thenReturn(1);

        CrmFollowUp voided = new CrmFollowUp();
        voided.setFollowUpId(6001L);
        voided.setIsVoided(true);
        when(followUpMapper.selectByFollowUpId("test-tenant", 6001L)).thenReturn(existing, voided);

        CrmFollowUp result = followUpService.void_(6001L, "误录");

        assertNotNull(result);
        verify(followUpMapper).markVoided("test-tenant", 6001L, "误录", "admin");
        verify(auditEventService).record(any());
    }

    @Test
    @DisplayName("查询详情 - 不存在时抛出异常")
    void testDetailNotFound()
    {
        TenantContext.setTenantId("test-tenant");
        when(followUpMapper.selectByFollowUpId("test-tenant", 9999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> followUpService.detail(9999L));
    }

    @Test
    @DisplayName("按客户查询跟进列表")
    void testListByCustomer()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUp f1 = new CrmFollowUp();
        f1.setFollowUpId(6001L);
        CrmFollowUp f2 = new CrmFollowUp();
        f2.setFollowUpId(6002L);

        when(followUpMapper.selectByCustomer("test-tenant", 1001L))
                .thenReturn(Arrays.asList(f1, f2));

        List<CrmFollowUp> result = followUpService.listByCustomer(1001L);

        assertEquals(2, result.size());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception
    {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
