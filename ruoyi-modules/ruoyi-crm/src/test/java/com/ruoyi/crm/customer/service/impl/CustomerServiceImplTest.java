package com.ruoyi.crm.customer.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.domain.CrmCustomerOwner;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.customer.mapper.CrmCustomerOwnerMapper;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 客户服务测试
 */
@DisplayName("客户服务测试")
class CustomerServiceImplTest
{
    private CrmCustomerMapper customerMapper;
    private CrmCustomerOwnerMapper ownerMapper;
    private IdGenerator idGenerator;
    private PermissionService permissionService;
    private AuditEventService auditEventService;
    private CustomerTimelineService timelineService;
    private CustomerServiceImpl customerService;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() throws Exception
    {
        customerMapper = Mockito.mock(CrmCustomerMapper.class);
        ownerMapper = Mockito.mock(CrmCustomerOwnerMapper.class);
        idGenerator = Mockito.mock(IdGenerator.class);
        permissionService = Mockito.mock(PermissionService.class);
        auditEventService = Mockito.mock(AuditEventService.class);
        timelineService = Mockito.mock(CustomerTimelineService.class);

        when(idGenerator.nextId()).thenReturn(1001L);
        when(permissionService.getScopeType(anyString(), anyLong())).thenReturn(ScopeType.ALL);
        doNothing().when(permissionService).check(any(PermissionContext.class));

        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(1L);
        loginUser.setUsername("admin");
        securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
        securityUtilsMock.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);

        customerService = new CustomerServiceImpl();

        setField(customerService, "customerMapper", customerMapper);
        setField(customerService, "ownerMapper", ownerMapper);
        setField(customerService, "idGenerator", idGenerator);
        setField(customerService, "permissionService", permissionService);
        setField(customerService, "auditEventService", auditEventService);
        setField(customerService, "timelineService", timelineService);
    }

    @AfterEach
    void tearDown()
    {
        securityUtilsMock.close();
        TenantContext.clear();
    }

    @Test
    @DisplayName("创建客户 - 正常流程成功")
    void testCreateCustomerSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setName("测试公司");
        customer.setPrimaryOwnerId(200L);
        customer.setPrimaryOwnerName("张三");
        customer.setNextFollowUpAt(new Date());

        when(customerMapper.selectByActiveNameKey("test-tenant", "测试公司")).thenReturn(null);
        when(customerMapper.insert(any(CrmCustomer.class))).thenReturn(1);
        when(ownerMapper.insert(any(CrmCustomerOwner.class))).thenReturn(1);

        CrmCustomer result = customerService.create(customer);

        assertNotNull(result);
        assertEquals(1001L, result.getCustomerId());
        assertEquals("test-tenant", result.getTenantId());
        assertEquals("正常", result.getOperatingStatus());
        assertEquals("新获取", result.getLifecycleStage());
        assertEquals("测试公司", result.getActiveNameKey());
        assertNotNull(result.getCustomerCode());
        assertEquals(Integer.valueOf(0), result.getVersion());

        verify(customerMapper).selectByActiveNameKey("test-tenant", "测试公司");
        verify(customerMapper).insert(any(CrmCustomer.class));
        verify(ownerMapper).insert(any(CrmCustomerOwner.class));
        verify(auditEventService).record(any());
        verify(timelineService).record(any());
    }

    @Test
    @DisplayName("创建客户 - 重名时抛出异常")
    void testCreateCustomerDuplicateName()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer existing = new CrmCustomer();
        existing.setCustomerId(999L);
        existing.setName("已存在公司");

        when(customerMapper.selectByActiveNameKey("test-tenant", "已存在公司")).thenReturn(existing);

        CrmCustomer customer = new CrmCustomer();
        customer.setName("已存在公司");
        customer.setNextFollowUpAt(new Date());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.create(customer));
        assertTrue(ex.getMessage().contains("客户名称已存在"));
    }

    @Test
    @DisplayName("创建客户 - 正常客户未设置下次跟进时间时抛出异常")
    void testCreateCustomerNoNextFollowUp()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setName("新公司");
        customer.setNextFollowUpAt(null);

        when(customerMapper.selectByActiveNameKey(anyString(), anyString())).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.create(customer));
        assertTrue(ex.getMessage().contains("下次跟进时间"));
    }

    @Test
    @DisplayName("查询客户列表 - 管理员返回全部")
    void testListAsAdmin()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer query = new CrmCustomer();
        query.setName("测试");

        List<CrmCustomer> mockResult = Arrays.asList(new CrmCustomer(), new CrmCustomer());
        when(customerMapper.selectVisibleList("test-tenant", query, "ALL", 1L, null))
                .thenReturn(mockResult);

        List<CrmCustomer> result = customerService.list(query);

        assertEquals(2, result.size());
        verify(customerMapper).selectVisibleList("test-tenant", query, "ALL", 1L, null);
    }

    @Test
    @DisplayName("查询客户详情 - 客户不存在时抛出异常")
    void testDetailNotFound()
    {
        TenantContext.setTenantId("test-tenant");
        when(customerMapper.selectByCustomerId("test-tenant", 9999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> customerService.detail(9999L));
    }

    @Test
    @DisplayName("编辑客户 - 乐观锁冲突时抛出异常")
    void testEditOptimisticLockConflict()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer existing = new CrmCustomer();
        existing.setCustomerId(1001L);
        existing.setTenantId("test-tenant");
        existing.setName("原名称");
        existing.setOperatingStatus("正常");
        existing.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(existing);
        when(customerMapper.update(any(CrmCustomer.class))).thenReturn(0);

        CrmCustomer edit = new CrmCustomer();
        edit.setCustomerId(1001L);
        edit.setName("新名称");
        edit.setVersion(0);

        assertThrows(IllegalStateException.class, () -> customerService.edit(edit));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception
    {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
