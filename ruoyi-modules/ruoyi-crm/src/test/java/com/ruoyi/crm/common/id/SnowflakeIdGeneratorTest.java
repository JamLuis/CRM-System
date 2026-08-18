package com.ruoyi.crm.common.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 雪花 ID 生成器测试
 */
@DisplayName("雪花ID生成器测试")
class SnowflakeIdGeneratorTest
{
    @Test
    @DisplayName("单线程连续生成 ID 单调递增")
    void testMonotonic()
    {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1, 1);
        long prev = gen.nextId();
        for (int i = 0; i < 1000; i++)
        {
            long curr = gen.nextId();
            assertTrue(curr > prev, "ID should be monotonically increasing: prev=" + prev + ", curr=" + curr);
            prev = curr;
        }
    }

    @Test
    @DisplayName("多线程并发生成 ID 全部唯一")
    void testConcurrency() throws InterruptedException
    {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1, 1);
        int threadCount = 16;
        int idsPerThread = 5000;
        Set<Long> allIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++)
        {
            pool.submit(() ->
            {
                try
                {
                    for (int i = 0; i < idsPerThread; i++)
                    {
                        allIds.add(gen.nextId());
                    }
                }
                finally
                {
                    latch.countDown();
                }
            });
        }
        latch.await();
        pool.shutdown();

        int expected = threadCount * idsPerThread;
        assertEquals(expected, allIds.size(), "All generated IDs must be unique");
    }

    @Test
    @DisplayName("不同 workerId 生成 ID 不同")
    void testDifferentWorker()
    {
        SnowflakeIdGenerator gen1 = new SnowflakeIdGenerator(1, 1);
        SnowflakeIdGenerator gen2 = new SnowflakeIdGenerator(2, 1);
        assertNotEquals(gen1.nextId(), gen2.nextId());
    }

    @Test
    @DisplayName("ID 为正数")
    void testPositive()
    {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1, 1);
        long id = gen.nextId();
        assertTrue(id > 0);
    }
}
