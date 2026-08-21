package com.ruoyi.crm.followup.worker;

import com.ruoyi.crm.followup.adapter.DingTalkReminderAdapter;
import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import com.ruoyi.crm.followup.domain.ReminderDeliveryStatus;
import com.ruoyi.crm.followup.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Date;
import java.util.List;

/**
 * 提醒投递 Worker
 * <p>
 * 定时轮询待调度/重试中的投递任务，通过钉钉适配器发送。
 * 每30秒扫描一次。
 *
 * @author ruoyi-crm
 */
@Component
@ConditionalOnProperty(name = "crm.reminder.legacy-worker.enabled", havingValue = "true")
public class ReminderDeliveryWorker
{
    private static final Logger log = LoggerFactory.getLogger(ReminderDeliveryWorker.class);

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private DingTalkReminderAdapter dingTalkAdapter;

    /**
     * 每30秒扫描待调度投递
     */
    @Scheduled(fixedDelay = 30000)
    public void dispatchPendingDeliveries()
    {
        Date now = new Date();
        List<CrmReminderDelivery> pending = reminderService.listPendingForDispatch(now);

        if (pending.isEmpty())
        {
            return;
        }

        log.info("Found {} pending deliveries to dispatch", pending.size());

        for (CrmReminderDelivery delivery : pending)
        {
            try
            {
                boolean success = dingTalkAdapter.send(delivery);

                if (success)
                {
                    reminderService.markSent(delivery.getDeliveryId(), "worker");
                }
                else
                {
                    // 发送失败，增加重试
                    CrmReminderDelivery updated = reminderService.incrementRetry(
                            delivery.getDeliveryId(), "SEND_FAILED", "worker");

                    if (updated != null
                            && ReminderDeliveryStatus.FAILED.name().equals(updated.getStatus()))
                    {
                        log.error("Delivery permanently failed: deliveryId={}, retryCount={}",
                                delivery.getDeliveryId(), updated.getRetryCount());
                    }
                }
            }
            catch (Exception e)
            {
                log.error("Error dispatching delivery {}: {}", delivery.getDeliveryId(), e.getMessage());
                reminderService.incrementRetry(delivery.getDeliveryId(),
                        "EXCEPTION:" + e.getClass().getSimpleName(), "worker");
            }
        }
    }
}
