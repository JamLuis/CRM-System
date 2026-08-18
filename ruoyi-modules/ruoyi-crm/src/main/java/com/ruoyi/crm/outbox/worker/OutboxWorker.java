package com.ruoyi.crm.outbox.worker;

import com.ruoyi.crm.outbox.domain.CrmOutbox;
import com.ruoyi.crm.outbox.service.OutboxService;
import com.ruoyi.crm.common.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Outbox Worker — 定时扫描待发送和失败重试消息
 * <p>
 * 重试策略：指数退避（5m → 30m → 2h），超过最大重试次数进入死信。
 * <p>
 * 幂等保证：投递器需自行保证幂等；worker 层通过乐观锁更新状态，
 * 多实例并发时只有一个实例能更新成功。
 */
@Component
public class OutboxWorker
{
    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);

    /** 重试间隔（毫秒）：5分钟、30分钟、2小时 */
    private static final long[] RETRY_INTERVALS = {
            TimeUnit.MINUTES.toMillis(5),
            TimeUnit.MINUTES.toMillis(30),
            TimeUnit.HOURS.toMillis(2)
    };

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private List<OutboxDispatcher> dispatchers;

    @Value("${crm.outbox.batch-size:50}")
    private int batchSize;

    @Value("${crm.outbox.max-retries:3}")
    private int maxRetries;

    @Value("${crm.outbox.tenant-id:default}")
    private String defaultTenantId;

    /**
     * 每 30 秒扫描一次待发送消息
     */
    @Scheduled(fixedDelayString = "${crm.outbox.poll-interval-ms:30000}")
    public void processPending()
    {
        processBatch(defaultTenantId, "PENDING");
    }

    /**
     * 每 60 秒扫描一次失败重试消息
     */
    @Scheduled(fixedDelayString = "${crm.outbox.retry-interval-ms:60000}")
    public void processRetry()
    {
        processRetryBatch(defaultTenantId);
    }

    private void processBatch(String tenantId, String phase)
    {
        try
        {
            List<CrmOutbox> messages = outboxService.findPending(tenantId, batchSize);
            if (messages.isEmpty())
            {
                return;
            }
            log.debug("OutboxWorker [{}] processing {} pending messages", phase, messages.size());
            for (CrmOutbox msg : messages)
            {
                dispatchOne(tenantId, msg);
            }
        }
        catch (Exception e)
        {
            log.error("OutboxWorker processPending error, tenant={}", tenantId, e);
        }
    }

    private void processRetryBatch(String tenantId)
    {
        try
        {
            List<CrmOutbox> messages = outboxService.findFailedForRetry(tenantId, maxRetries, batchSize);
            if (messages.isEmpty())
            {
                return;
            }
            log.debug("OutboxWorker retry processing {} failed messages", messages.size());
            for (CrmOutbox msg : messages)
            {
                dispatchOne(tenantId, msg);
            }
        }
        catch (Exception e)
        {
            log.error("OutboxWorker processRetry error, tenant={}", tenantId, e);
        }
    }

    private void dispatchOne(String tenantId, CrmOutbox msg)
    {
        boolean success = false;
        try
        {
            OutboxDispatcher dispatcher = findDispatcher(msg.getEventType());
            if (dispatcher == null)
            {
                log.warn("No dispatcher for event type: {}, marking as DEAD, id={}", msg.getEventType(), msg.getId());
                outboxService.updateStatus(msg.getId(), tenantId, "DEAD", msg.getRetryCount(), null, msg.getVersion());
                return;
            }
            success = dispatcher.dispatch(msg);
        }
        catch (Exception e)
        {
            log.error("Dispatch error, id={}, eventType={}", msg.getId(), msg.getEventType(), e);
        }

        int newRetryCount = msg.getRetryCount() + (success ? 0 : 1);
        Date nextRetry = success ? null : computeNextRetry(newRetryCount);
        String newStatus = success ? "SENT" : (newRetryCount >= maxRetries ? "DEAD" : "FAILED");

        boolean updated = outboxService.updateStatus(
                msg.getId(), tenantId, newStatus, newRetryCount, nextRetry, msg.getVersion()
        );
        if (!updated)
        {
            log.debug("Outbox message already processed by another instance, id={}", msg.getId());
        }
    }

    private Date computeNextRetry(int retryCount)
    {
        int idx = Math.min(retryCount - 1, RETRY_INTERVALS.length - 1);
        long delay = RETRY_INTERVALS[idx];
        return new Date(System.currentTimeMillis() + delay);
    }

    private OutboxDispatcher findDispatcher(String eventType)
    {
        if (dispatchers == null)
        {
            return null;
        }
        for (OutboxDispatcher d : dispatchers)
        {
            if (d.supports(eventType))
            {
                return d;
            }
        }
        return null;
    }

    @PostConstruct
    public void init()
    {
        log.info("OutboxWorker initialized, batchSize={}, maxRetries={}, tenant={}", batchSize, maxRetries, defaultTenantId);
    }
}
