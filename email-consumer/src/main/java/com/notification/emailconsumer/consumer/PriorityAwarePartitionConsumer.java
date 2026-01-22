package com.notification.emailconsumer.consumer;

import com.notification.emailconsumer.service.MessageHandlerService;
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

import static com.notification.emailconsumer.constants.Constants.TOPIC;
import static com.notification.emailconsumer.constants.Constants.GROUP_ID;

/**
 * Priority-Aware Partition Consumer
 * 
 * CORE LOGIC - PARTITION-LEVEL PRIORITY ENFORCEMENT:
 * 
 * 1. Manual Partition Assignment:
 *    - Subscribes to all 3 partitions of email-topic
 *    - Partition 0: Priority 1 (highest)
 *    - Partition 1: Priority 2 (medium)
 *    - Partition 2: Priority 3 (lowest)
 * 
 * 2. Infinite Polling Loop:
 *    - Continuously checks pending messages in each partition
 *    - Uses endOffsets vs current position to calculate lag
 * 
 * 3. Smart Pause/Resume Strategy:
 *    IF partition 0 has messages:
 *      → PAUSE partitions 1 & 2
 *      → Process ONLY partition 0
 *    ELSE IF partition 1 has messages:
 *      → PAUSE partition 2
 *      → RESUME partition 1
 *      → Process partition 1
 *    ELSE:
 *      → RESUME all partitions
 *      → Process partition 2
 * 
 * This ensures high-priority emails are ALWAYS sent before low-priority ones!
 */
@Component
@Slf4j
public class PriorityAwarePartitionConsumer {
    private ConsumerFactory consumerFactory;
    private MessageHandlerService messageHandlerService;

    public PriorityAwarePartitionConsumer(ConsumerFactory consumerFactory, MessageHandlerService messageHandlerService){
        this.consumerFactory = consumerFactory;
        this.messageHandlerService = messageHandlerService;
    }

    @EventListener
    public void onAppStarted(ApplicationStartedEvent applicationStartedEvent){
         KafkaConsumer<String, String> consumer = (KafkaConsumer<String, String>) consumerFactory.createConsumer(GROUP_ID,"email-consumer");

        TopicPartition PARTITION_PRIORITY_1 = new TopicPartition(TOPIC,0);
        TopicPartition PARTITION_PRIORITY_2 = new TopicPartition(TOPIC,1);
        TopicPartition PARTITION_PRIORITY_3 = new TopicPartition(TOPIC,2);

        // Assign partitions manually (not subscribe) for fine-grained control
        consumer.assign(Arrays.asList(
                PARTITION_PRIORITY_1,
                PARTITION_PRIORITY_2,
                PARTITION_PRIORITY_3
        ));

        while(true){
            try {
                // Fetch end offsets for all partitions
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Arrays.asList(
                        PARTITION_PRIORITY_1, PARTITION_PRIORITY_2, PARTITION_PRIORITY_3
                ));

                // Get current positions
                long positionP1 = consumer.position(PARTITION_PRIORITY_1);
                long positionP2 = consumer.position(PARTITION_PRIORITY_2);
                long positionP3 = consumer.position(PARTITION_PRIORITY_3);

                // Check offsets for priority-based pausing/resuming
                boolean hasPriority1Messages = (endOffsets.get(PARTITION_PRIORITY_1) - positionP1) > 0;

                if (hasPriority1Messages) {
                    // Pause lower-priority partitions
                    consumer.pause(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                } else {
                    boolean hasPriority2Messages = (endOffsets.get(PARTITION_PRIORITY_2) - positionP2) > 0;

                    if (hasPriority2Messages) {
                        // Pause the lowest-priority partition
                        consumer.pause(Arrays.asList(PARTITION_PRIORITY_3));
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2));
                    } else {
                        // Resume all partitions if no priority 1 or 2 messages are left
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                    }
                }

                // Poll for new messages from active partitions
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(record -> processRecord(record)); // Process messages
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processRecord(ConsumerRecord record) {
        log.debug("Record Received: \n"+"Offset: " + record.offset()
                + ", Key: " + record.key() + ", Value: " + record.value());
        processMessage(record.value().toString());
    }

    private void processMessage(String message) {
        //process messages as if you are listening to fresh kafka topic
        //The above code will handle prioritization among partitions
        messageHandlerService.handleEmailRequest(message);
    }
}
