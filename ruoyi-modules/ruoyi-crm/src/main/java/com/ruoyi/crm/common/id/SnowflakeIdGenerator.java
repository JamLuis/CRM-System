package com.ruoyi.crm.common.id;

/**
 * 雪花 ID 生成器（Twitter Snowflake 算法）
 * <p>
 * 结构：1 bit 符号位 | 41 bit 时间戳 | 10 bit 机器位 | 12 bit 序列号
 * <p>
 * 机器位拆分为 5 bit dataCenterId + 5 bit workerId，可自行配置。
 * 时间戳精度为毫秒，可用约 69 年。
 *
 * @author ruoyi-crm
 */
public class SnowflakeIdGenerator
{
    /** 起始时间戳 (2023-01-01 00:00:00 UTC) */
    private static final long START_TIMESTAMP = 1672531200000L;

    /** 机器 ID 占用的位数 */
    private static final long WORKER_ID_BITS = 5L;

    /** 数据中心 ID 占用的位数 */
    private static final long DATA_CENTER_ID_BITS = 5L;

    /** 最大机器 ID (31) */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 最大数据中心 ID (31) */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /** 序列号占用的位数 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器 ID 左移位数 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 数据中心 ID 左移位数 */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 时间戳左移位数 */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /** 序列号掩码 (4095) */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long workerId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     *
     * @param workerId     机器 ID (0-31)
     * @param dataCenterId 数据中心 ID (0-31)
     */
    public SnowflakeIdGenerator(long workerId, long dataCenterId)
    {
        if (workerId < 0 || workerId > MAX_WORKER_ID)
        {
            throw new IllegalArgumentException(
                    String.format("workerId 必须在 0-%d 之间", MAX_WORKER_ID));
        }
        if (dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID)
        {
            throw new IllegalArgumentException(
                    String.format("dataCenterId 必须在 0-%d 之间", MAX_DATA_CENTER_ID));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 生成下一个 ID
     *
     * @return 雪花 ID
     */
    public synchronized long nextId()
    {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp < lastTimestamp)
        {
            throw new RuntimeException(
                    String.format("时钟回拨 %d 毫秒", lastTimestamp - currentTimestamp));
        }

        if (currentTimestamp == lastTimestamp)
        {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0L)
            {
                // 当前毫秒序列号耗尽，等待下一毫秒
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        }
        else
        {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp)
    {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp)
        {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
