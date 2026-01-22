package com.notification.priority3_processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.priority3_processor.models.NotificationRequest;
import com.notification.priority3_processor.service.NotificationProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.notification.priority3_processor.constants.Constants.TOPIC_PRIORITY_3;

/**
 * Kafka Consumer for Priority 3 Notifications
 * 
 * CORE LOGIC:
 * 1. Listens to "priority-3" Kafka topic using @KafkaListener
 * 2. Deserializes JSON message to NotificationRequest object
 * 3. Delegates processing to NotificationProcessingService
 * 4. Handles exceptions gracefully with logging
 * 
 * FLOW:
 * Kafka "priority-3" topic → consumeNotificationRequest() → NotificationProcessingService
 * 
 * ERROR HANDLING:
 * - JsonProcessingException: Invalid JSON format
 * - General Exception: Processing failures logged without crashing consumer
 */
@Component
@Slf4j
public class KafkaPriority3Consumer {
    NotificationProcessingService notificationProcessingService;
    
    public KafkaPriority3Consumer(NotificationProcessingService notificationProcessingService){
        this.notificationProcessingService = notificationProcessingService;
    }
    
    @KafkaListener(topics = TOPIC_PRIORITY_3)
    public void consumeNotificationRequest(String notificationRequestString){
        ObjectMapper mapper = new ObjectMapper();

        try{
            // Step 1: Parse JSON string to JsonNode
            JsonNode notificationRequestJson = mapper.readTree(notificationRequestString);
            
            // Step 2: Convert JsonNode to NotificationRequest object
            NotificationRequest notificationRequest = mapper.treeToValue(notificationRequestJson, NotificationRequest.class);
            log.debug("Successfully parsed Consumed Notification Request: {}", notificationRequest.toString());
            
            try{
                // Step 3: Process the notification (save to DB, send to channel topics)
                notificationProcessingService.processNotification(notificationRequest);
            } catch (Exception exception){
                log.error("Unexpected Exception in NotificationProcessingService while processing Notification Request: {}", notificationRequest);
                log.error("Exception: {}", exception.toString());
            }
        } catch (JsonProcessingException jsonProcessingException){
            log.error("Error parsing kafka consumed message to JSON. Exception: \n {}", jsonProcessingException.toString());
        }
    }
}
