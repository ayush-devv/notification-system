package com.notification.notification_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import static com.notification.notification_service.constants.Constants.*;

@Configuration
public class KafkaConfig {

    // Create Kafka topics automatically when application starts
    // If topics already exist, this won't recreate them
    @Bean
    public KafkaAdmin.NewTopics createTopics() {
        
        // Create topic for high priority notifications (OTP, Password Reset)
        NewTopic priority1Topic = TopicBuilder
                .name(TOPIC_PRIORITY_1)  // "priority-1"
                .build();
        
        // Create topic for medium priority notifications (Welcome, Updates)
        NewTopic priority2Topic = TopicBuilder
                .name(TOPIC_PRIORITY_2)  // "priority-2"
                .build();
        
        // Create topic for low priority notifications (Promotional, Newsletters)
        NewTopic priority3Topic = TopicBuilder
                .name(TOPIC_PRIORITY_3)  // "priority-3"
                .build();

        // Return all topics to be created
        return new KafkaAdmin.NewTopics(priority1Topic, priority2Topic, priority3Topic);
    }
}
