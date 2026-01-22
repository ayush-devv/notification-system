package com.notification.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.notification_service.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.notification.notification_service.constants.Constants.*;

@Service
@Slf4j
public class KafkaService {

    // STEP 1: Declare KafkaTemplate dependency
    // KafkaTemplate is Spring's abstraction for sending messages to Kafka topics
    // <String, String> means: Key type is String, Value type is String
       private final KafkaTemplate<String,String>kafkaTemplate;

    
    
    // STEP 2: Constructor injection
    // Spring will automatically inject KafkaTemplate when creating this service
       public KafkaService(KafkaTemplate<String,String>kafkaTemplate){
              this.kafkaTemplate = kafkaTemplate;

       }
    
    
    
    // METHOD 1: sendNotification() 
    // Purpose: Route notification to appropriate Kafka topic based on priority
    // Parameters: NotificationRequest object containing priority and notification data
    // Returns: void (throws KafkaException on failure)
    public void sendNotification(NotificationRequest notificationRequest){
        try{
            String notification=prepareMessage(notificationRequest);
            int priority=notificationRequest.getNotificationPriority();
    // STEP 3: Create public void sendNotification(NotificationRequest notificationRequest) method
            switch (priority){
                case 1:
                    this.kafkaTemplate.send(TOPIC_PRIORITY_1,notification);
                    break;
                case 2:
                    this.kafkaTemplate.send(TOPIC_PRIORITY_2,notification);
                    break;
                default:
                    this.kafkaTemplate.send(TOPIC_PRIORITY_3,notification);
            }
            log.info("Notification Successfully forwarded to Kafka with priority: " + priority);
        } catch (Exception e) {
            throw new KafkaException("Failed to send notification", e);
        }
    }
       
    
    
    

    //  METHOD 2: prepareMessage() 
    // Purpose: Convert NotificationRequest object to JSON string for Kafka
    // Parameters: NotificationRequest object
    // Returns: String (JSON representation)
    // Throws: JsonProcessingException if serialization fails
    private String prepareMessage(NotificationRequest notificationRequest) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(notificationRequest);
    }

    
}
