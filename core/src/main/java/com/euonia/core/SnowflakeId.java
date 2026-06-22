package com.euonia.core;

import java.util.ResourceBundle;

/**
 * SnowflakeId 是一个分布式唯一 ID 生成器，灵感来源于 Twitter 的 Snowflake 算法。
 * 它基于当前时间戳、工作节点 ID、数据中心 ID 和序列号生成 64 位唯一 ID。
 * 生成的 ID 可按时间排序，且可在分布式环境中生成，无需节点间协调。
 * 生成的 ID 结构如下：
 * - 41 位用于时间戳（自自定义纪元以来的毫秒数）
 * - 5 位用于数据中心 ID
 * - 5 位用于工作节点 ID
 * - 12 位用于序列号
 * 此实现允许每个工作节点每毫秒生成最多 1024 个唯一 ID，并支持最多 32 个数据中心，每个数据中心最多 32 个工作节点。
 * 自定义纪元设置为 2021 年 1 月 1 日，这使得 ID 生成具有较长的使用寿命，而不会耗尽时间戳的位数。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class SnowflakeId {
    private static final ResourceBundle resource = ResourceBundle.getBundle("core");

    private static final long EPOCH = 1609459200000L; // 2021-01-01 00:00:00 UTC
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private SnowflakeId(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format(resource.getString("SnowflakeId.WorkIdOverflowException"), MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format(resource.getString("SnowflakeId.DatacenterIdOverflowException"), MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public static synchronized SnowflakeId getInstance(long workerId, long datacenterId) {
        return new SnowflakeId(workerId, datacenterId);
    }

    public static synchronized SnowflakeId getInstance() {
        return new SnowflakeId(0, 0);
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(resource.getString("SnowflakeId.ClockBackwardException"), lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
