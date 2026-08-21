package com.ruoyi.crm.followup.adapter;

import com.ruoyi.crm.dingtalk.client.DingTalkClient;
import com.ruoyi.crm.followup.domain.CrmReminderDelivery;
import com.ruoyi.crm.tenant.domain.CrmDingtalkIdentity;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkIdentityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

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

    @Autowired
    private CrmDingtalkIdentityMapper identityMapper;

    @Autowired
    private DingTalkClient dingTalkClient;

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
        CrmDingtalkIdentity identity = identityMapper.selectBySysUserId(
                delivery.getTenantId(), delivery.getRecipientUserId());
        if (identity == null)
        {
            throw new IllegalStateException("接收人没有有效的钉钉身份映射");
        }
        String plannedAt = delivery.getPlannedFollowUpAt() == null ? "-"
                : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(delivery.getPlannedFollowUpAt());
        String customerName = delivery.getCustomerName() == null
                ? String.valueOf(delivery.getCustomerId()) : delivery.getCustomerName();
        String content = "CRM跟进提醒：客户「" + customerName + "」计划跟进时间为 " + plannedAt + "。";
        Long taskId = dingTalkClient.sendWorkNotification(identity.getDingtalkUserId(), content);
        log.info("DingTalk reminder sent: deliveryId={}, taskId={}", delivery.getDeliveryId(), taskId);
        return taskId != null;
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
