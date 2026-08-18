package com.ruoyi.crm.followup.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.followup.domain.CrmFollowUpStatusStrategy;
import com.ruoyi.crm.followup.domain.FollowUpStatus;
import com.ruoyi.crm.followup.mapper.CrmFollowUpStatusStrategyMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 跟进状态（健康度）服务测试
 */
@DisplayName("跟进状态（健康度）服务测试")
class FollowUpStatusServiceImplTest
{
    private CrmFollowUpStatusStrategyMapper strategyMapper;
    private CrmCustomerMapper customerMapper;
    private IdGenerator idGenerator;
    private FollowUpStatusServiceImpl statusService;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() throws Exception
    {
        strategyMapper = Mockito.mock(CrmFollowUpStatusStrategyMapper.class);
        customerMapper = Mockito.mock(CrmCustomerMapper.class);
        idGenerator = Mockito.mock(IdGenerator.class);

        when(idGenerator.nextId()).thenReturn(8001L);

        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");

        statusService = new FollowUpStatusServiceImpl();
        setField(statusService, "strategyMapper", strategyMapper);
        setField(statusService, "customerMapper", customerMapper);
        setField(statusService, "idGenerator", idGenerator);
    }

    @AfterEach
    void tearDown()
    {
        securityUtilsMock.close();
        TenantContext.clear();
    }

    @Test
    @DisplayName("计算状态 - 正常客户最近跟进在阈值内返回 NORMAL")
    void testCalculateNormal()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");
        customer.setLastEffectiveFollowUpAt(new Date()); // 今天
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(7);
        strategy.setSevereThreshold(14);
        when(strategyMapper.selectActive("test-tenant")).thenReturn(strategy);

        String result = statusService.calculate(1001L);

        assertEquals(FollowUpStatus.NORMAL.name(), result);
        verify(customerMapper).updateFollowUpStatus(eq("test-tenant"), eq(1001L),
                eq(FollowUpStatus.NORMAL.name()), eq("system"), eq(0));
    }

    @Test
    @DisplayName("计算状态 - 超过不足阈值返回 INSUFFICIENT")
    void testCalculateInsufficient()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");

        // 10天前跟进
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        customer.setLastEffectiveFollowUpAt(cal.getTime());
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(7);
        strategy.setSevereThreshold(14);
        when(strategyMapper.selectActive("test-tenant")).thenReturn(strategy);

        String result = statusService.calculate(1001L);

        assertEquals(FollowUpStatus.INSUFFICIENT.name(), result);
    }

    @Test
    @DisplayName("计算状态 - 超过严重不足阈值返回 SEVERE_INSUFFICIENT")
    void testCalculateSevereInsufficient()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");

        // 20天前跟进
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -20);
        customer.setLastEffectiveFollowUpAt(cal.getTime());
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(7);
        strategy.setSevereThreshold(14);
        when(strategyMapper.selectActive("test-tenant")).thenReturn(strategy);

        String result = statusService.calculate(1001L);

        assertEquals(FollowUpStatus.SEVERE_INSUFFICIENT.name(), result);
    }

    @Test
    @DisplayName("计算状态 - 暂停跟进客户返回 NOT_ASSESSED")
    void testCalculatePausedCustomerReturnsNotAssessed()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("暂停跟进");
        customer.setFollowUpStatus(FollowUpStatus.NORMAL.name());

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        String result = statusService.calculate(1001L);

        assertEquals(FollowUpStatus.NORMAL.name(), result);
        // 不应更新状态
        verify(customerMapper, never()).updateFollowUpStatus(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("计算状态 - 已失效客户返回 NOT_ASSESSED")
    void testCalculateInvalidCustomerReturnsNotAssessed()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("已失效");

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        String result = statusService.calculate(1001L);

        assertEquals(FollowUpStatus.NOT_ASSESSED.name(), result);
    }

    @Test
    @DisplayName("计算状态 - 客户不存在时抛出异常")
    void testCalculateCustomerNotFound()
    {
        TenantContext.setTenantId("test-tenant");
        when(customerMapper.selectByCustomerId("test-tenant", 9999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> statusService.calculate(9999L));
    }

    @Test
    @DisplayName("计算状态 - 无策略时返回 NOT_ASSESSED")
    void testCalculateNoStrategyReturnsNotAssessed()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");
        customer.setLastEffectiveFollowUpAt(new Date());

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);
        when(strategyMapper.selectActive("test-tenant")).thenReturn(null);

        String result = statusService.calculate(1001L);

        assertEquals(FollowUpStatus.NOT_ASSESSED.name(), result);
    }

    @Test
    @DisplayName("计算状态 - 新客户无跟进记录使用创建时间")
    void testCalculateNewCustomerUsesCreateTime()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setOperatingStatus("正常");
        customer.setLastEffectiveFollowUpAt(null); // 无跟进记录
        customer.setCreateTime(new Date()); // 今天创建
        customer.setVersion(0);

        when(customerMapper.selectByCustomerId("test-tenant", 1001L)).thenReturn(customer);

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(7);
        strategy.setSevereThreshold(14);
        when(strategyMapper.selectActive("test-tenant")).thenReturn(strategy);

        String result = statusService.calculate(1001L);

        assertEquals(FollowUpStatus.NORMAL.name(), result);
    }

    @Test
    @DisplayName("批量重算 - 只重算 NORMAL 状态客户")
    void testRecalculateBatch()
    {
        TenantContext.setTenantId("test-tenant");

        CrmCustomer c1 = new CrmCustomer();
        c1.setCustomerId(1001L);
        c1.setOperatingStatus("正常");
        c1.setLastEffectiveFollowUpAt(new Date());
        c1.setVersion(0);

        CrmCustomer c2 = new CrmCustomer();
        c2.setCustomerId(1002L);
        c2.setOperatingStatus("正常");
        c2.setLastEffectiveFollowUpAt(new Date());
        c2.setVersion(0);

        when(customerMapper.selectIdsByFollowUpStatus("test-tenant", FollowUpStatus.NORMAL.name()))
                .thenReturn(Arrays.asList(c1, c2));
        when(customerMapper.selectByCustomerId(eq("test-tenant"), anyLong()))
                .thenAnswer(invocation -> {
                    Long cid = invocation.getArgument(1);
                    CrmCustomer c = new CrmCustomer();
                    c.setCustomerId(cid);
                    c.setOperatingStatus("正常");
                    c.setLastEffectiveFollowUpAt(new Date());
                    c.setVersion(0);
                    return c;
                });

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(7);
        strategy.setSevereThreshold(14);
        when(strategyMapper.selectActive("test-tenant")).thenReturn(strategy);

        int count = statusService.recalculateBatch();

        assertEquals(2, count);
    }

    @Test
    @DisplayName("保存策略 - 阈值校验通过")
    void testSaveStrategySuccess()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(7);
        strategy.setSevereThreshold(14);

        when(strategyMapper.insert(any(CrmFollowUpStatusStrategy.class))).thenReturn(1);
        when(customerMapper.selectIdsByFollowUpStatus("test-tenant", FollowUpStatus.NORMAL.name()))
                .thenReturn(java.util.Collections.emptyList());

        CrmFollowUpStatusStrategy result = statusService.saveStrategy(strategy);

        assertNotNull(result);
        assertEquals(8001L, result.getStrategyId());
        assertEquals("ACTIVE", result.getStatus());
        verify(strategyMapper).insert(any(CrmFollowUpStatusStrategy.class));
        verify(strategyMapper).deactivateOld("test-tenant", 8001L, "admin");
    }

    @Test
    @DisplayName("保存策略 - 不足阈值 < 1 时抛出异常")
    void testSaveStrategyInsufficientThresholdTooLow()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(0);
        strategy.setSevereThreshold(14);

        assertThrows(IllegalArgumentException.class, () -> statusService.saveStrategy(strategy));
    }

    @Test
    @DisplayName("保存策略 - 严重阈值 <= 不足阈值时抛出异常")
    void testSaveStrategySevereNotGreaterThanInsufficient()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(7);
        strategy.setSevereThreshold(7); // 相等

        assertThrows(IllegalArgumentException.class, () -> statusService.saveStrategy(strategy));
    }

    @Test
    @DisplayName("保存策略 - 阈值超过 365 时抛出异常")
    void testSaveStrategyThresholdExceeds365()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setInsufficientThreshold(400);
        strategy.setSevereThreshold(500);

        assertThrows(IllegalArgumentException.class, () -> statusService.saveStrategy(strategy));
    }

    @Test
    @DisplayName("查询当前生效策略")
    void testGetActiveStrategy()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUpStatusStrategy strategy = new CrmFollowUpStatusStrategy();
        strategy.setStrategyId(8001L);
        strategy.setStatus("ACTIVE");
        when(strategyMapper.selectActive("test-tenant")).thenReturn(strategy);

        CrmFollowUpStatusStrategy result = statusService.getActiveStrategy();

        assertNotNull(result);
        assertEquals(8001L, result.getStrategyId());
    }

    @Test
    @DisplayName("查询全部策略列表")
    void testListStrategies()
    {
        TenantContext.setTenantId("test-tenant");

        CrmFollowUpStatusStrategy s1 = new CrmFollowUpStatusStrategy();
        s1.setStrategyId(8001L);
        CrmFollowUpStatusStrategy s2 = new CrmFollowUpStatusStrategy();
        s2.setStrategyId(8002L);

        when(strategyMapper.selectAll("test-tenant")).thenReturn(Arrays.asList(s1, s2));

        List<CrmFollowUpStatusStrategy> result = statusService.listStrategies();

        assertEquals(2, result.size());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception
    {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
