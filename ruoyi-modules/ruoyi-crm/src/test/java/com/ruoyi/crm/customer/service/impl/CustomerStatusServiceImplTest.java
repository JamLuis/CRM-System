package com.ruoyi.crm.customer.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.system.api.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 客户经营状态机服务测试
 */
@DisplayName("客户经营状态机服务测试")
class CustomerStatusServiceImplTest
{
    private CrmCustomerMapper customerMapper;
    private PermissionService permissionService;
    private AuditEventService auditEventService;
    private CustomerTimelineService timelineService;
    private CustomerStatusServiceImpl statusService;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() throws Exception
    {
        customerMapper = Mockito.mock(CrmCustomerMapper.class);
        permissionService = Mockito.mock(PermissionService.class);
        auditEventService = Mockito.mock(AuditEventService.class);
        timelineService = Mockito.mock(CustomerTimelineService.class);

        doNothing().when(permissionService).check(any(PermissionContext.class));

        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(1L);
        loginUser.setUsername("admin");
        securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
        securityUtilsMock.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);

        statusService = new CustomerStatusServiceImpl();

        setField(statusService, "customerMapper", customerMapper);
        setField(statusService, "permissionService", permissionService);
        setField(statusService, "auditEventService", auditEventService);
        setField(statusService, "timelineService", timelineService);
    }

    @AfterEach
    void tearDown()
    {
        securityUtilsMock.close();
        TenantContext.clear();
    }

    @Test
    @DisplayName("暂停客户 - 正常→暂停跟进 成功")
    void testPauseSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(customerMapper.updateOperatingStatus(eq("test-tenant"), eq(1001L), eq("暂停跟进"),
                anyString(), any(Date.class), isNull(), anyString(), eq(0)))
                .thenReturn(1);

        statusService.pause(1001L, "暂停原因", new Date());

        verify(customerMapper).updateOperatingStatus(eq("test-tenant"), eq(1001L), eq("暂停跟进"),
                anyString(), any(Date.class), isNull(), anyString(), eq(0));
    }

    @Test
    @DisplayName("暂停客户 - 非正常状态时抛出异常")
    void testPauseInvalidStatus()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("已失效");

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        assertThrows(IllegalStateException.class,
                () -> statusService.pause(1001L, "暂停原因", new Date()));
    }

    @Test
    @DisplayName("恢复客户 - 暂停跟进→正常 成功")
    void testResumeSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("暂停跟进");
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(customerMapper.updateOperatingStatus(eq("test-tenant"), eq(1001L), eq("正常"),
                anyString(), isNull(), isNull(), anyString(), eq(0)))
                .thenReturn(1);

        statusService.resume(1001L, "恢复原因");

        verify(customerMapper).updateOperatingStatus(eq("test-tenant"), eq(1001L), eq("正常"),
                anyString(), isNull(), isNull(), anyString(), eq(0));
    }

    @Test
    @DisplayName("恢复客户 - 非暂停状态时抛出异常")
    void testResumeInvalidStatus()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        assertThrows(IllegalStateException.class, () -> statusService.resume(1001L, "恢复原因"));
    }

    @Test
    @DisplayName("失效客户 - 正常→已失效 成功")
    void testInvalidateSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(customerMapper.updateOperatingStatus(eq("test-tenant"), eq(1001L), eq("已失效"),
                anyString(), isNull(), isNull(), anyString(), eq(0)))
                .thenReturn(1);

        statusService.invalidate(1001L, "失效原因");

        verify(customerMapper).updateOperatingStatus(eq("test-tenant"), eq(1001L), eq("已失效"),
                anyString(), isNull(), isNull(), anyString(), eq(0));
    }

    @Test
    @DisplayName("归档客户 - 已归档状态时抛出异常")
    void testArchiveAlreadyArchived()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("已归档");

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        assertThrows(IllegalStateException.class, () -> statusService.archive(1001L, "归档原因"));
    }

    @Test
    @DisplayName("乐观锁冲突 - 更新返回0行时抛出异常")
    void testOptimisticLockConflict()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(customerMapper.updateOperatingStatus(anyString(), anyLong(), anyString(),
                anyString(), any(), any(), anyString(), anyInt()))
                .thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> statusService.pause(1001L, "暂停原因", new Date()));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception
    {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
