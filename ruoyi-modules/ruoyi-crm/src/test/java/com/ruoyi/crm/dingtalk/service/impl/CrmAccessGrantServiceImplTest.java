package com.ruoyi.crm.dingtalk.service.impl;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.crm.dingtalk.service.DingTalkIdentityExchangeService;
import com.ruoyi.crm.tenant.domain.CrmDingtalkDirectoryUser;
import com.ruoyi.crm.tenant.domain.CrmDingtalkIdentity;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkDirectoryUserMapper;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkIdentityMapper;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CRM 访问授权边界测试")
class CrmAccessGrantServiceImplTest
{
    private CrmDingtalkDirectoryUserMapper directoryMapper;
    private CrmDingtalkIdentityMapper identityMapper;
    private DingTalkIdentityExchangeService identityService;
    private RemoteUserService remoteUserService;
    private CrmAccessGrantServiceImpl service;

    @BeforeEach
    void setUp() throws Exception
    {
        directoryMapper = mock(CrmDingtalkDirectoryUserMapper.class);
        identityMapper = mock(CrmDingtalkIdentityMapper.class);
        identityService = mock(DingTalkIdentityExchangeService.class);
        remoteUserService = mock(RemoteUserService.class);
        service = new CrmAccessGrantServiceImpl();
        setField("directoryUserMapper", directoryMapper);
        setField("identityMapper", identityMapper);
        setField("identityExchangeService", identityService);
        setField("remoteUserService", remoteUserService);

        when(directoryMapper.selectByDingtalkUserId("default", "ding-user"))
                .thenReturn(directoryUser());
        when(directoryMapper.selectAssignableRoles())
                .thenReturn(Collections.singletonList(role(10L)));
    }

    @Test
    @DisplayName("拒绝分配不含 crm:access 的角色")
    void rejectsNonCrmRole()
    {
        assertThrows(ServiceException.class,
                () -> service.grant("default", "ding-user", Collections.singletonList(999L)));

        verify(remoteUserService, never()).innerAddUser(any(), anyString());
        verify(remoteUserService, never()).innerAuthRoles(anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("更新 CRM 角色时保留用户原有非 CRM 角色")
    void preservesExistingNonCrmRoles()
    {
        CrmDingtalkIdentity identity = new CrmDingtalkIdentity();
        identity.setSysUserId(42L);
        when(identityMapper.selectByDingtalkUserId("default", "ding-user")).thenReturn(identity);

        SysUser existing = new SysUser();
        existing.setUserId(42L);
        existing.setRoles(Arrays.asList(role(10L), role(77L)));
        when(remoteUserService.innerGetUserById(eq(42L), anyString())).thenReturn(R.ok(existing));
        when(remoteUserService.innerEditUser(any(SysUser.class), anyString())).thenReturn(R.ok(true));
        when(remoteUserService.innerAuthRoles(eq(42L), any(Long[].class), anyString()))
                .thenReturn(R.ok(true));

        service.grant("default", "ding-user", Collections.singletonList(10L));

        ArgumentCaptor<Long[]> roles = ArgumentCaptor.forClass(Long[].class);
        verify(remoteUserService).innerAuthRoles(eq(42L), roles.capture(), anyString());
        assertArrayEquals(new Long[]{77L, 10L}, roles.getValue());
    }

    private CrmDingtalkDirectoryUser directoryUser()
    {
        CrmDingtalkDirectoryUser user = new CrmDingtalkDirectoryUser();
        user.setDingtalkUserId("ding-user");
        user.setName("测试人员");
        user.setActive(true);
        return user;
    }

    private SysRole role(Long roleId)
    {
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        return role;
    }

    private void setField(String name, Object value) throws Exception
    {
        Field field = CrmAccessGrantServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }
}
