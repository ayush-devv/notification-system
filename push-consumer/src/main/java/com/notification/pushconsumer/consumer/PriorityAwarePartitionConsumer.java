package com.notification.pushconsumer.consumer;

import com.notification.pushconsumer.service.MessageHandlerService;
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

import static com.notification.pushconsumer.constants.Constants.GROUP_ID;
import static com.notification.pushconsumer.constants.Constants.TOPIC;

/**
 * PriorityAwarePartitionConsumer - Implements partition-level priority enforcement for push notifications
 * 
 * Architecture:
 * - push-topic has 3 partitions: 0 (priority1), 1 (priority2), 2 (priority3)
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
 * This ensures high-priority push notifications are always processed before lower priorities.
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

    @EventListener
    public void onAppStarted(ApplicationStartedEvent applicationStartedEvent) {
        KafkaConsumer<String, String> consumer = (KafkaConsumer<String, String>) consumerFactory.createConsumer(GROUP_ID, "push-consumer");

        TopicPartition PARTITION_PRIORITY_1 = new TopicPartition(TOPIC, 0);
        TopicPartition PARTITION_PRIORITY_2 = new TopicPartition(TOPIC, 1);
        TopicPartition PARTITION_PRIORITY_3 = new TopicPartition(TOPIC, 2);

        consumer.assign(Arrays.asList(
                PARTITION_PRIORITY_1,
                PARTITION_PRIORITY_2,
                PARTITION_PRIORITY_3
        ));

        while (true) {
            try {
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Arrays.asList(
                        PARTITION_PRIORITY_1, PARTITION_PRIORITY_2, PARTITION_PRIORITY_3
                ));

                long positionP1 = consumer.position(PARTITION_PRIORITY_1);
                long positionP2 = consumer.position(PARTITION_PRIORITY_2);
                long positionP3 = consumer.position(PARTITION_PRIORITY_3);

                boolean hasPriority1Messages = (endOffsets.get(PARTITION_PRIORITY_1) - positionP1) > 0;

                if (hasPriority1Messages) {
                    consumer.pause(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                    log.debug("Priority 1 messages detected. Pausing Priority 2 and 3.");
                } else {
                    boolean hasPriority2Messages = (endOffsets.get(PARTITION_PRIORITY_2) - positionP2) > 0;

                    if (hasPriority2Messages) {
                        consumer.pause(Arrays.asList(PARTITION_PRIORITY_3));
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2));
                        log.debug("Priority 2 messages detected. Pausing Priority 3.");
                    } else {
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                        log.debug("No high priority messages. Processing all partitions.");
                    }
                }

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(record -> processRecord(record));
            } catch (Exception e) {
                log.error("Error in priority-aware consumer: {}", e.getMessage(), e);
            }
        }
    }

    private void processRecord(ConsumerRecord record) {
        log.debug("Record Received: \n" + "Offset: " + record.offset()
                + ", Key: " + record.key() + ", Value: " + record.value());
        processMessage(record.value().toString());
    }

    private void processMessage(String message) {
        messageHandlerService.handlePushRequest(message);
    }
}
