package com.ruoyi.crm.outbox.worker;

import com.ruoyi.crm.outbox.domain.CrmOutbox;
import com.ruoyi.crm.outbox.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * OutboxWorker 重试与死信策略测试
 */
@DisplayName("OutboxWorker 重试与死信测试")
class OutboxWorkerTest
{
    private OutboxService outboxService;
    private OutboxDispatcher successDispatcher;
    private OutboxDispatcher failDispatcher;
    private OutboxWorker worker;

    @BeforeEach
    void setup()
    {
        outboxService = Mockito.mock(OutboxService.class);
        when(outboxService.claimForDelivery(anyLong(), anyString(), anyInt())).thenReturn(true);
        successDispatcher = Mockito.mock(OutboxDispatcher.class);
        when(successDispatcher.supports(anyString())).thenReturn(true);
        when(successDispatcher.dispatch(any())).thenReturn(true);

        failDispatcher = Mockito.mock(OutboxDispatcher.class);
        when(failDispatcher.supports(anyString())).thenReturn(true);
        when(failDispatcher.dispatch(any())).thenReturn(false);

        worker = new OutboxWorker();
        try
        {
            java.lang.reflect.Field svcField = OutboxWorker.class.getDeclaredField("outboxService");
            svcField.setAccessible(true);
            svcField.set(worker, outboxService);

            java.lang.reflect.Field bsField = OutboxWorker.class.getDeclaredField("batchSize");
            bsField.setAccessible(true);
            bsField.set(worker, 50);

            java.lang.reflect.Field mrField = OutboxWorker.class.getDeclaredField("maxRetries");
            mrField.setAccessible(true);
            mrField.set(worker, 3);

            java.lang.reflect.Field timeoutField = OutboxWorker.class.getDeclaredField("sendingTimeoutSeconds");
            timeoutField.setAccessible(true);
            timeoutField.set(worker, 300);

            java.lang.reflect.Field dtField = OutboxWorker.class.getDeclaredField("defaultTenantId");
            dtField.setAccessible(true);
            dtField.set(worker, "default");
        }
        catch (Exception e)
        {
            fail("Failed to inject fields: " + e.getMessage());
        }
    }

    private void setDispatchers(OutboxDispatcher... dispatchers)
    {
        try
        {
            java.lang.reflect.Field dispField = OutboxWorker.class.getDeclaredField("dispatchers");
            dispField.setAccessible(true);
            dispField.set(worker, Arrays.asList(dispatchers));
        }
        catch (Exception e)
        {
            fail("Failed to set dispatchers: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("无待发送消息时不执行投递")
    void testNoPendingMessages()
    {
        setDispatchers(successDispatcher);
        when(outboxService.findPending("default", 50)).thenReturn(Collections.emptyList());
        worker.processPending();
        verify(outboxService, never()).updateStatus(any(), any(), any(), anyInt(), any(), anyInt());
    }

    @Test
    @DisplayName("投递成功后状态更新为 SENT")
    void testDispatchSuccess()
    {
        setDispatchers(successDispatcher);
        CrmOutbox msg = createOutbox(1L, 0, 0);
        when(outboxService.findPending("default", 50)).thenReturn(Collections.singletonList(msg));
        when(outboxService.updateStatus(eq(1L), eq("default"), anyString(), anyInt(), any(), eq(1)))
                .thenReturn(true);

        worker.processPending();

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxService).updateStatus(eq(1L), eq("default"), statusCaptor.capture(), eq(0), any(), eq(1));
        assertEquals("SENT", statusCaptor.getValue());
    }

    @Test
    @DisplayName("投递失败后状态更新为 FAILED 并设置重试时间")
    void testDispatchFailure()
    {
        setDispatchers(failDispatcher);
        CrmOutbox msg = createOutbox(1L, 0, 0);
        when(outboxService.findPending("default", 50)).thenReturn(Collections.singletonList(msg));
        when(outboxService.updateStatus(any(), any(), any(), anyInt(), any(), anyInt())).thenReturn(true);

        worker.processPending();

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> retryCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outboxService).updateStatus(eq(1L), eq("default"), statusCaptor.capture(), retryCaptor.capture(), any(), eq(1));
        assertEquals("FAILED", statusCaptor.getValue());
        assertEquals(1, retryCaptor.getValue());
    }

    @Test
    @DisplayName("超过最大重试次数后进入死信")
    void testDeadLetter()
    {
        setDispatchers(failDispatcher);
        // retryCount=2, maxRetries=3 → 失败后 retryCount=3 >= 3 → DEAD
        CrmOutbox msg = createOutbox(1L, 2, 0);
        when(outboxService.findFailedForRetry("default", 3, 50)).thenReturn(Collections.singletonList(msg));
        when(outboxService.updateStatus(any(), any(), any(), anyInt(), any(), anyInt())).thenReturn(true);

        worker.processRetry();

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxService).updateStatus(eq(1L), eq("default"), statusCaptor.capture(), eq(3), any(), eq(1));
        assertEquals("DEAD", statusCaptor.getValue());
    }

    @Test
    @DisplayName("无匹配 dispatcher 时标记为 DEAD")
    void testNoDispatcher()
    {
        setDispatchers(); // 空列表
        CrmOutbox msg = createOutbox(1L, 0, 0);
        when(outboxService.findPending("default", 50)).thenReturn(Collections.singletonList(msg));
        when(outboxService.updateStatus(any(), any(), any(), anyInt(), any(), anyInt())).thenReturn(true);

        worker.processPending();

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxService).updateStatus(eq(1L), eq("default"), statusCaptor.capture(), eq(0), any(), eq(1));
        assertEquals("DEAD", statusCaptor.getValue());
    }

    @Test
    @DisplayName("未抢占到消息时不重复投递")
    void testClaimLostSkipsDispatch()
    {
        setDispatchers(successDispatcher);
        CrmOutbox msg = createOutbox(1L, 0, 0);
        when(outboxService.findPending("default", 50)).thenReturn(Collections.singletonList(msg));
        when(outboxService.claimForDelivery(1L, "default", 0)).thenReturn(false);

        worker.processPending();

        verify(successDispatcher, never()).dispatch(any());
        verify(outboxService, never()).updateStatus(any(), any(), any(), anyInt(), any(), anyInt());
    }

    private CrmOutbox createOutbox(Long id, int retryCount, int version)
    {
        CrmOutbox msg = new CrmOutbox();
        msg.setId(id);
        msg.setTenantId("default");
        msg.setEventType("TEST_EVENT");
        msg.setAggregateType("TestAggregate");
        msg.setAggregateId("1");
        msg.setPayload("{}");
        msg.setStatus("PENDING");
        msg.setRetryCount(retryCount);
        msg.setVersion(version);
        return msg;
    }
}
