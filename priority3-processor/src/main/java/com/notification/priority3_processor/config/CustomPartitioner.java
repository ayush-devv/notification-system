package com.notification.priority3_processor.config;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * Custom Kafka Partitioner for Priority 3 Notifications
 * 
 * PARTITION STRATEGY:
 * - Priority 1 → Partition 0 (highest priority)
 * - Priority 2 → Partition 1 (medium priority)
 * - Priority 3 → Partition 2 (lowest priority)
 * 
 * PURPOSE:
 * This ensures that when notifications are sent to channel topics (email-topic, sms-topic),
 * they go to partition 2. The channel consumers (EmailConsumer, SMSConsumer) can then
 * implement partition-level priority by pausing partition 2 when processing partitions 0 and 1.
 * 
 * HOW IT WORKS:
 * 1. priority3-processor produces messages to channel topics
 * 2. This partitioner ensures they go to partition 2
 * 3. Channel consumers process partition 0 first, then 1, then 2
 * 4. This enforces priority at the channel level
 */
public class CustomPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // Always return partition 2 for priority 3 notifications
        return 2;
    }

    @Override
    public void close() {
        // Perform any necessary cleanup
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // Perform any necessary configuration
    }
}
