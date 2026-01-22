package com.notification.smsconsumer.consumer;

import com.notification.smsconsumer.service.MessageHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import static com.notification.smsconsumer.constants.Constants.GROUP_ID;
import static com.notification.smsconsumer.constants.Constants.TOPIC;

/**
 * PriorityAwarePartitionConsumer - Implements partition-level priority enforcement for SMS notifications
 * 
 * Architecture:
 * - sms-topic has 3 partitions: 0 (priority1), 1 (priority2), 2 (priority3)
 * - Manually assigns all 3 partitions instead of using subscribe()
 * - Dynamically pauses/resumes partitions based on pending messages
 * 
 * Priority Logic:
 * 1. If partition 0 (priority1) has messages:
 *    - Pause partitions 1 and 2 (process only priority1)
 * 2. Else if partition 1 (priority2) has messages:
 *    - Resume partition 1, pause partition 2 (process only priority2)
 * 3. Else:
 *    - Resume all partitions (process priority3)
 * 
 * How it works:
 * - Uses endOffsets() to check if messages are pending in each partition
 * - Compares endOffset with current position to determine pending count
 * - Pauses lower priority partitions when higher priority has messages
 * - Runs in infinite loop checking priorities before each poll
 * 
 * This ensures high-priority SMS are always processed before lower priorities
 * even if lower priority messages arrived first.
 */
@Component
@Slf4j
public class PriorityAwarePartitionConsumer {
    private ConsumerFactory consumerFactory;
    private MessageHandlerService messageHandlerService;

    public PriorityAwarePartitionConsumer(ConsumerFactory consumerFactory, MessageHandlerService messageHandlerService) {
        this.consumerFactory = consumerFactory;
        this.messageHandlerService = messageHandlerService;
    }

    /**
     * Starts consuming when application is ready
     * Triggered by Spring's ApplicationStartedEvent
     */
    @EventListener
    public void onAppStarted(ApplicationStartedEvent applicationStartedEvent) {
        KafkaConsumer<String, String> consumer = (KafkaConsumer<String, String>) consumerFactory.createConsumer(GROUP_ID, "sms-consumer");

        // Define the 3 partitions for priority levels
        TopicPartition PARTITION_PRIORITY_1 = new TopicPartition(TOPIC, 0);
        TopicPartition PARTITION_PRIORITY_2 = new TopicPartition(TOPIC, 1);
        TopicPartition PARTITION_PRIORITY_3 = new TopicPartition(TOPIC, 2);

        // Manually assign all partitions (not subscribe - we need manual control)
        consumer.assign(Arrays.asList(
                PARTITION_PRIORITY_1,
                PARTITION_PRIORITY_2,
                PARTITION_PRIORITY_3
        ));

        // Infinite loop to continuously process messages with priority awareness
        while (true) {
            try {
                // Fetch end offsets for all partitions to know where messages end
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Arrays.asList(
                        PARTITION_PRIORITY_1, PARTITION_PRIORITY_2, PARTITION_PRIORITY_3
                ));

                // Get current positions (where we're currently reading from)
                long positionP1 = consumer.position(PARTITION_PRIORITY_1);
                long positionP2 = consumer.position(PARTITION_PRIORITY_2);
                long positionP3 = consumer.position(PARTITION_PRIORITY_3);

                // Check if each partition has pending messages
                // Pending count = endOffset - currentPosition
                boolean hasPriority1Messages = (endOffsets.get(PARTITION_PRIORITY_1) - positionP1) > 0;

                if (hasPriority1Messages) {
                    // Priority 1 has messages - pause lower priorities
                    consumer.pause(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                    log.debug("Priority 1 messages detected. Pausing Priority 2 and 3.");
                } else {
                    // No priority 1 messages - check priority 2
                    boolean hasPriority2Messages = (endOffsets.get(PARTITION_PRIORITY_2) - positionP2) > 0;

                    if (hasPriority2Messages) {
                        // Priority 2 has messages - pause only priority 3
                        consumer.pause(Arrays.asList(PARTITION_PRIORITY_3));
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2));
                        log.debug("Priority 2 messages detected. Pausing Priority 3.");
                    } else {
                        // No priority 1 or 2 messages - process priority 3
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                        log.debug("No high priority messages. Processing all partitions.");
                    }
                }

                // Poll for new messages from active (non-paused) partitions
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(record -> processRecord(record));
            } catch (Exception e) {
                log.error("Error in priority-aware consumer: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Process individual Kafka record
     */
    private void processRecord(ConsumerRecord record) {
        log.debug("Record Received: \n" + "Offset: " + record.offset()
                + ", Key: " + record.key() + ", Value: " + record.value());
        processMessage(record.value().toString());
    }

    /**
     * Delegate message processing to MessageHandlerService
     */
    private void processMessage(String message) {
        messageHandlerService.handleSmsRequest(message);
    }
}
