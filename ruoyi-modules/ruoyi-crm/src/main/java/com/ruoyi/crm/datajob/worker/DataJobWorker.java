package com.ruoyi.crm.datajob.worker;

import com.ruoyi.crm.datajob.domain.CrmDataJob;
import com.ruoyi.crm.datajob.domain.DataJobStatus;
import com.ruoyi.crm.datajob.mapper.CrmDataJobMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 数据作业定时任务
 * <p>
 * 定时将超过保留期的导出作业标记为 EXPIRED（下载过期）。
 *
 * @author ruoyi-crm
 */
@Component
public class DataJobWorker
{
    private static final Logger log = LoggerFactory.getLogger(DataJobWorker.class);

    @Autowired
    private CrmDataJobMapper dataJobMapper;

    /**
     * 过期标记：每 10 分钟扫描一次超过保留期的导出作业
     */
    @Scheduled(fixedDelayString = "${crm.datajob.expire-scan-interval-ms:600000}")
    public void markExpiredExports()
    {
        List<CrmDataJob> expired = dataJobMapper.selectExpiredExports(new Date());
        if (expired == null || expired.isEmpty())
        {
            return;
        }
        for (CrmDataJob job : expired)
        {
            job.setStatus(DataJobStatus.EXPIRED.name());
            job.setUpdateBy("system");
            dataJobMapper.update(job);
        }
        log.info("Marked {} expired export job(s)", expired.size());
    }
}
