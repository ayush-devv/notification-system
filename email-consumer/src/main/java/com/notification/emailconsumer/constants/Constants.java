package com.notification.emailconsumer.constants;

/**
 * Constants for Email Consumer Service
 * 
 * KAFKA CONFIGURATION:
 * - TOPIC: The Kafka topic this consumer listens to (email-topic)
 * - GROUP_ID: Consumer group identifier for offset management
 * 
 * PARTITION STRATEGY:
 * The email-topic has 3 partitions:
 * - Partition 0: Priority 1 messages (from priority1-processor)
 * - Partition 1: Priority 2 messages (from priority2-processor)
 * - Partition 2: Priority 3 messages (from priority3-processor)
 * 
 * The PriorityAwarePartitionConsumer uses these constants to
 * subscribe to all partitions and enforce priority-based processing.
 */
public class Constants {
    // Kafka topic containing email notifications from all priority processors
    public static final String TOPIC = "email-topic";
    
    // Consumer group ID for tracking offset across restarts
    public static final String GROUP_ID = "email-consumer";
}
