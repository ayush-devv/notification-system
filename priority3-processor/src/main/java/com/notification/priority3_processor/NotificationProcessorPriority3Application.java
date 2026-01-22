package com.notification.priority3_processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Priority 3 Notification Processor
 * 
 * ARCHITECTURE:
 * This is a Kafka consumer application that:
 * 1. Listens to "priority-3" Kafka topic
 * 2. Processes low-priority notifications
 * 3. Sends processed notifications to channel-specific topics (email, sms, push)
 * 
 * PRIORITY SYSTEM:
 * Priority 3 = Lowest priority (processed last by channel consumers)
 * - Marketing emails, newsletters
 * - Non-urgent announcements
 * - Batch notifications
 */
@SpringBootApplication
public class NotificationProcessorPriority3Application {

	public static void main(String[] args) {
		SpringApplication.run(NotificationProcessorPriority3Application.class, args);
	}

}
