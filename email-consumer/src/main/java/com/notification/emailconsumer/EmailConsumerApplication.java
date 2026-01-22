package com.notification.emailconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Email Consumer Service - Main Application
 * 
 * PURPOSE:
 * Consumes email notifications from the email-topic (3 partitions)
 * and sends them via SendGrid email service.
 * 
 * PARTITION-LEVEL PRIORITY:
 * - Partition 0: Priority 1 (highest) - processed first
 * - Partition 1: Priority 2 (medium) - processed after P0
 * - Partition 2: Priority 3 (lowest) - processed last
 * 
 * The PriorityAwarePartitionConsumer implements smart pause/resume
 * logic to enforce priority at the partition level.
 */
@SpringBootApplication
public class EmailConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailConsumerApplication.class, args);
	}

}
