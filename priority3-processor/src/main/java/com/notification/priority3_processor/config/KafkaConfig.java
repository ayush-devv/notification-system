package com.notification.priority3_processor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import static com.notification.priority3_processor.constants.Constants.*;

/**
 * Kafka Configuration for Channel Topics
 * 
 * CREATES 3 CHANNEL TOPICS:
 * 1. email-topic (3 partitions for priority-based processing)
 * 2. sms-topic (3 partitions for priority-based processing)
 * 3. push-n-topic (3 partitions for push notifications)
 * 
 * PARTITION LAYOUT:
 * Each topic has 3 partitions:
 * - Partition 0: Priority 1 messages (highest priority)
 * - Partition 1: Priority 2 messages (medium priority)
 * - Partition 2: Priority 3 messages (lowest priority)
 * 
 * WHY 3 PARTITIONS?
 * This enables partition-level priority enforcement in channel consumers.
 * EmailConsumer/SMSConsumer can pause/resume partitions to process
 * high-priority messages before low-priority ones.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public KafkaAdmin.NewTopics createTopic(){
        NewTopic smsTopic = TopicBuilder
                .name(SMS_TOPIC)
                .partitions(3)
                .build();
        NewTopic emailTopic = TopicBuilder
                .name(EMAIL_TOPIC)
                .partitions(3)
                .build();
        NewTopic pushNTopic = TopicBuilder
                .name(PUSH_N_TOPIC)
                .partitions(3)
                .build();

        return new KafkaAdmin.NewTopics(smsTopic, emailTopic, pushNTopic);
    }
}
