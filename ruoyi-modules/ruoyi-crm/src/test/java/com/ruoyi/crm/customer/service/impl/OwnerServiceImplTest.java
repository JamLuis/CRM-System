package com.ruoyi.crm.customer.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.domain.CrmCustomerOwner;
import com.ruoyi.crm.customer.domain.CrmOwnerChange;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.customer.mapper.CrmCustomerOwnerMapper;
import com.ruoyi.crm.customer.mapper.CrmOwnerChangeMapper;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 成员维护与移交服务测试
 */
@DisplayName("成员维护与移交服务测试")
class OwnerServiceImplTest
{
    private CrmCustomerMapper customerMapper;
    private CrmCustomerOwnerMapper ownerMapper;
    private CrmOwnerChangeMapper ownerChangeMapper;
    private IdGenerator idGenerator;
    private PermissionService permissionService;
    private AuditEventService auditEventService;
    private CustomerTimelineService timelineService;
    private OwnerServiceImpl ownerService;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() throws Exception
    {
        customerMapper = Mockito.mock(CrmCustomerMapper.class);
        ownerMapper = Mockito.mock(CrmCustomerOwnerMapper.class);
        ownerChangeMapper = Mockito.mock(CrmOwnerChangeMapper.class);
        idGenerator = Mockito.mock(IdGenerator.class);
        permissionService = Mockito.mock(PermissionService.class);
        auditEventService = Mockito.mock(AuditEventService.class);
        timelineService = Mockito.mock(CustomerTimelineService.class);

        doNothing().when(permissionService).check(any(PermissionContext.class));
        when(idGenerator.nextId()).thenReturn(5001L);

        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(1L);
        loginUser.setUsername("admin");
        securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
        securityUtilsMock.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);

        ownerService = new OwnerServiceImpl();

        setField(ownerService, "customerMapper", customerMapper);
        setField(ownerService, "ownerMapper", ownerMapper);
        setField(ownerService, "changeMapper", ownerChangeMapper);
        setField(ownerService, "idGenerator", idGenerator);
        setField(ownerService, "permissionService", permissionService);
        setField(ownerService, "auditEventService", auditEventService);
        setField(ownerService, "timelineService", timelineService);
    }

    @AfterEach
    void tearDown()
    {
        securityUtilsMock.close();
        TenantContext.clear();
    }

    @Test
    @DisplayName("移交客户 - 正常流程成功")
    void testTransferSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setPrimaryOwnerId(200L);
        customer.setPrimaryOwnerName("张三");
        customer.setOperatingStatus("正常");
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(customerMapper.updatePrimaryOwner(eq("test-tenant"), eq(1001L), eq(201L),
                eq("李四"), anyLong(), anyString(), anyString(), eq(0)))
                .thenReturn(1);
        when(ownerMapper.deactivateByCustomerAndRole("test-tenant", 1001L, "PRIMARY", "admin"))
                .thenReturn(1);
        when(ownerMapper.insert(any(CrmCustomerOwner.class))).thenReturn(1);
        when(ownerChangeMapper.insert(any(CrmOwnerChange.class))).thenReturn(1);

        ownerService.transfer(1001L, 201L, "李四", 10L, true, "人员调整");

        verify(customerMapper).updatePrimaryOwner(eq("test-tenant"), eq(1001L), eq(201L),
                eq("李四"), anyLong(), anyString(), anyString(), eq(0));
        verify(ownerMapper).deactivateByCustomerAndRole("test-tenant", 1001L, "PRIMARY", "admin");
        // 保留为协同人 + 新主负责人 = 2次 insert
        verify(ownerMapper, times(2)).insert(any(CrmCustomerOwner.class));
        verify(ownerChangeMapper).insert(any(CrmOwnerChange.class));
    }

    @Test
    @DisplayName("移交客户 - 乐观锁冲突时抛出异常")
    void testTransferOptimisticLockConflict()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setPrimaryOwnerId(200L);
        customer.setOperatingStatus("正常");
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(customerMapper.updatePrimaryOwner(anyString(), anyLong(), anyLong(),
                anyString(), anyLong(), anyString(), anyString(), anyInt()))
                .thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> ownerService.transfer(1001L, 201L, "李四", 10L, false, "测试"));
    }

    @Test
    @DisplayName("添加协同人 - 正常流程成功")
    void testAddCollaboratorSuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setCollaboratorIds("201,202");
        customer.setOperatingStatus("正常");
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(ownerMapper.selectActiveCollaborators("test-tenant", 1001L))
                .thenReturn(Collections.emptyList());
        when(ownerMapper.insert(any(CrmCustomerOwner.class))).thenReturn(1);
        when(customerMapper.update(any(CrmCustomer.class))).thenReturn(1);
        when(ownerChangeMapper.insert(any(CrmOwnerChange.class))).thenReturn(1);

        ownerService.addCollaborator(1001L, 203L, "王五");

        verify(ownerMapper).insert(any(CrmCustomerOwner.class));
        verify(customerMapper).updatePrimaryOwner(eq("test-tenant"), eq(1001L),
                isNull(), isNull(), isNull(), eq("201,202,203"), anyString(), eq(0));
        verify(ownerChangeMapper).insert(any(CrmOwnerChange.class));
    }

    @Test
    @DisplayName("查询成员列表 - 返回主负责人和协同人")
    void testListMembers()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomerOwner primary = new CrmCustomerOwner();
        primary.setRoleType("PRIMARY");
        primary.setUserName("张三");

        CrmCustomerOwner collaborator = new CrmCustomerOwner();
        collaborator.setRoleType("COLLABORATOR");
        collaborator.setUserName("李四");

        when(ownerMapper.selectActiveByCustomer("test-tenant", 1001L))
                .thenReturn(Arrays.asList(primary, collaborator));

        List<CrmCustomerOwner> members = ownerService.listMembers(1001L);

        assertEquals(2, members.size());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception
    {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
