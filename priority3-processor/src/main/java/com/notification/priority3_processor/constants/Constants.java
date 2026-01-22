package com.notification.priority3_processor.constants;

/**
 * Constants for Priority 3 Processor
 * 
 * KEY CONCEPTS:
 * 1. TOPIC_PRIORITY_3: The Kafka topic this processor listens to
 * 2. PRIORITY: The priority level (3 = lowest priority)
 * 3. Channel topics: Where processed notifications are sent (email-topic, sms-topic, push-n-topic)
 * 
 * HOW IT WORKS:
 * - notification-service routes priority=3 requests to "priority-3" topic
 * - This processor consumes from "priority-3" topic
 * - After processing, sends to channel-specific topics (email-topic, etc.)
 * - Channel consumers (EmailConsumer, etc.) implement partition-level priority
 */
public class Constants {
    // Kafka topic for priority 3 notifications
    public static final String TOPIC_PRIORITY_3 = "priority-3";
    
    // Key used for custom partitioning strategy
    public static final String PRIORITY_KEY_FOR_PARTITIONS = "priority-3";
    
    // Priority level (3 = lowest priority, processed last)
    public static final int PRIORITY = 3;
    
    // Channel-specific Kafka topics for sending processed notifications
    public static final String SMS_TOPIC = "sms-topic";
    public static final String EMAIL_TOPIC = "email-topic";
    public static final String PUSH_N_TOPIC = "push-n-topic";
}
