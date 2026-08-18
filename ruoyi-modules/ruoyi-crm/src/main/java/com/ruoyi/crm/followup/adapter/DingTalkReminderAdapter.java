package com.ruoyi.crm.followup.adapter;

import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 钉钉提醒适配器
 * <p>
 * 负责通过钉钉工作通知发送跟进提醒。
 * 重试策略：5分钟、30分钟、2小时。
 *
 * @author ruoyi-crm
 */
@Component
public class DingTalkReminderAdapter
{
    private static final Logger log = LoggerFactory.getLogger(DingTalkReminderAdapter.class);

    /** 重试间隔（毫秒）：5分钟、30分钟、2小时 */
    private static final long[] RETRY_INTERVALS = {
            5 * 60 * 1000L,
            30 * 60 * 1000L,
            2 * 60 * 60 * 1000L
    };

    /**
     * 发送钉钉提醒
     *
     * @param delivery 投递任务
     * @return true=发送成功，false=发送失败
     */
    public boolean send(CrmReminderDelivery delivery)
    {
        log.info("Sending DingTalk reminder: deliveryId={}, recipientUserId={}, customerId={}",
                delivery.getDeliveryId(), delivery.getRecipientUserId(), delivery.getCustomerId());

        // TODO: 实际调用钉钉开放平台工作通知 API
        // 目前模拟发送成功
        return true;
    }

    /**
     * 计算下次重试时间
     *
     * @param retryCount 当前重试次数
     * @return 下次重试时间间隔（毫秒），超过最大重试次数返回 -1
     */
    public long calculateRetryInterval(int retryCount)
    {
        if (retryCount >= RETRY_INTERVALS.length)
        {
            return -1;
        }
        return RETRY_INTERVALS[retryCount];
    }
}
