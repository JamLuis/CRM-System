package com.ruoyi.crm.followup.service.impl;

import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import com.ruoyi.crm.followup.domain.CrmReminderPlan;
import com.ruoyi.crm.followup.domain.ReminderDeliveryStatus;
import com.ruoyi.crm.followup.domain.ReminderPlanStatus;
import com.ruoyi.crm.followup.mapper.CrmReminderDeliveryMapper;
import com.ruoyi.crm.followup.mapper.CrmReminderPlanMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 提醒服务测试
 */
@DisplayName("提醒服务测试")
class ReminderServiceImplTest
{
    private CrmReminderPlanMapper planMapper;
    private CrmReminderDeliveryMapper deliveryMapper;
    private IdGenerator idGenerator;
    private ReminderServiceImpl reminderService;

    @BeforeEach
    void setup() throws Exception
    {
        planMapper = Mockito.mock(CrmReminderPlanMapper.class);
        deliveryMapper = Mockito.mock(CrmReminderDeliveryMapper.class);
        idGenerator = Mockito.mock(IdGenerator.class);

        when(idGenerator.nextId()).thenReturn(7001L, 7002L);

        reminderService = new ReminderServiceImpl();
        setField(reminderService, "planMapper", planMapper);
        setField(reminderService, "deliveryMapper", deliveryMapper);
        setField(reminderService, "idGenerator", idGenerator);
    }

    @AfterEach
    void tearDown()
    {
        TenantContext.clear();
    }

    @Test
    @DisplayName("创建计划 - 取消旧计划并创建新计划和投递")
    void testCreatePlanCancelsOldAndCreatesNew()
    {
        TenantContext.setTenantId("test-tenant");

        // 计划跟进时间为明天
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date plannedFollowUpAt = cal.getTime();

        when(planMapper.insert(any(CrmReminderPlan.class))).thenReturn(1);
        when(deliveryMapper.insert(any(CrmReminderDelivery.class))).thenReturn(1);

        CrmReminderPlan result = reminderService.createPlan(6001L, 1001L,
                plannedFollowUpAt, 1L, "admin");

        assertNotNull(result);
        assertEquals(7001L, result.getPlanId());
        assertEquals("test-tenant", result.getTenantId());
        assertEquals(1001L, result.getCustomerId());
        assertEquals(6001L, result.getSourceFollowUpId());
        assertEquals("FU-6001", result.getPlanKey());
        assertEquals(ReminderPlanStatus.ACTIVE.name(), result.getStatus());

        // 验证取消旧计划
        verify(planMapper).cancelActiveByCustomer("test-tenant", 1001L, "system");
        verify(deliveryMapper).cancelByCustomer("test-tenant", 1001L, "system");

        // 验证创建新计划和投递
        verify(planMapper).insert(any(CrmReminderPlan.class));
        verify(deliveryMapper).insert(any(CrmReminderDelivery.class));
    }

    @Test
    @DisplayName("创建计划 - 计划时间已过则立即发送")
    void testCreatePlanScheduledAtInPastSendsImmediately()
    {
        TenantContext.setTenantId("test-tenant");

        // 计划跟进时间为昨天（已过）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date plannedFollowUpAt = cal.getTime();

        when(planMapper.insert(any(CrmReminderPlan.class))).thenReturn(1);
        when(deliveryMapper.insert(any(CrmReminderDelivery.class))).thenReturn(1);

        CrmReminderPlan result = reminderService.createPlan(6001L, 1001L,
                plannedFollowUpAt, 1L, "admin");

        // scheduledAt 应该是当前时间附近（立即发送）
        Date now = new Date();
        long diff = now.getTime() - result.getScheduledAt().getTime();
        assertTrue(Math.abs(diff) < 5000, "scheduledAt 应为当前时间");
    }

    @Test
    @DisplayName("按客户取消 - 调用 mapper 取消计划和投递")
    void testCancelByCustomer()
    {
        TenantContext.setTenantId("test-tenant");

        reminderService.cancelByCustomer(1001L, "manual");

        verify(planMapper).cancelActiveByCustomer("test-tenant", 1001L, "manual");
        verify(deliveryMapper).cancelByCustomer("test-tenant", 1001L, "manual");
    }

    @Test
    @DisplayName("标记发送成功")
    void testMarkSent()
    {
        TenantContext.setTenantId("test-tenant");

        reminderService.markSent(7002L, "system");

        verify(deliveryMapper).updateStatus(eq("test-tenant"), eq(7002L),
                eq(ReminderDeliveryStatus.SENT.name()), any(), isNull(), isNull(), eq("system"));
    }

    @Test
    @DisplayName("标记完成")
    void testMarkCompleted()
    {
        TenantContext.setTenantId("test-tenant");

        reminderService.markCompleted(7002L, "system");

        verify(deliveryMapper).updateStatus(eq("test-tenant"), eq(7002L),
                eq(ReminderDeliveryStatus.COMPLETED.name()), isNull(), isNull(), any(), eq("system"));
    }

    @Test
    @DisplayName("标记失败")
    void testMarkFailed()
    {
        TenantContext.setTenantId("test-tenant");

        reminderService.markFailed(7002L, "TIMEOUT", "system");

        verify(deliveryMapper).updateStatus(eq("test-tenant"), eq(7002L),
                eq(ReminderDeliveryStatus.FAILED.name()), any(), eq("TIMEOUT"), isNull(), eq("system"));
    }

    @Test
    @DisplayName("重试递增 - 未达上限时递增重试次数")
    void testIncrementRetryBelowLimit()
    {
        TenantContext.setTenantId("test-tenant");

        when(deliveryMapper.incrementRetry(eq("test-tenant"), eq(7002L),
                any(), eq("TIMEOUT"), eq("system"))).thenReturn(1);

        CrmReminderDelivery delivery = new CrmReminderDelivery();
        delivery.setDeliveryId(7002L);
        delivery.setRetryCount(1);
        when(deliveryMapper.selectByDeliveryId("test-tenant", 7002L)).thenReturn(delivery);

        CrmReminderDelivery result = reminderService.incrementRetry(7002L, "TIMEOUT", "system");

        assertNotNull(result);
        assertEquals(1, result.getRetryCount());
        verify(deliveryMapper).incrementRetry(eq("test-tenant"), eq(7002L),
                any(), eq("TIMEOUT"), eq("system"));
        // 不应标记为 FAILED
        verify(deliveryMapper, never()).updateStatus(eq("test-tenant"), eq(7002L),
                eq(ReminderDeliveryStatus.FAILED.name()), any(), any(), any(), any());
    }

    @Test
    @DisplayName("重试递增 - 达上限时标记为 FAILED")
    void testIncrementRetryAtLimitMarksFailed()
    {
        TenantContext.setTenantId("test-tenant");

        // incrementRetry 返回 0 表示已达上限
        when(deliveryMapper.incrementRetry(eq("test-tenant"), eq(7002L),
                any(), eq("TIMEOUT"), eq("system"))).thenReturn(0);

        CrmReminderDelivery delivery = new CrmReminderDelivery();
        delivery.setDeliveryId(7002L);
        when(deliveryMapper.selectByDeliveryId("test-tenant", 7002L)).thenReturn(delivery);

        CrmReminderDelivery result = reminderService.incrementRetry(7002L, "TIMEOUT", "system");

        assertNotNull(result);
        // 应标记为 FAILED
        verify(deliveryMapper).updateStatus(eq("test-tenant"), eq(7002L),
                eq(ReminderDeliveryStatus.FAILED.name()), any(), eq("TIMEOUT"), isNull(), eq("system"));
    }

    @Test
    @DisplayName("查询待调度投递列表")
    void testListPendingForDispatch()
    {
        TenantContext.setTenantId("test-tenant");

        Date now = new Date();
        CrmReminderDelivery d1 = new CrmReminderDelivery();
        d1.setDeliveryId(7002L);
        CrmReminderDelivery d2 = new CrmReminderDelivery();
        d2.setDeliveryId(7003L);

        when(deliveryMapper.selectPendingForDispatch("test-tenant", now))
                .thenReturn(Arrays.asList(d1, d2));

        List<CrmReminderDelivery> result = reminderService.listPendingForDispatch(now);

        assertEquals(2, result.size());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception
    {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
