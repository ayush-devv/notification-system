package com.notification.pushconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.pushconsumer.models.PushRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * MessageHandlerService - Handles rate limiting and JSON deserialization for push notification requests
 * 
 * Responsibilities:
 * 1. Rate limiting: Enforces 600 push notifications/minute limit
 * 2. JSON deserialization: Parses Kafka message string to PushRequest object
 * 3. Delegates to PushProcessingService for actual push notification sending
 */
@Service
@Slf4j
public class MessageHandlerService {
    ObjectMapper mapper;
    PushProcessingService pushProcessingService;

    public MessageHandlerService(ObjectMapper mapper, PushProcessingService pushProcessingService) {
        this.mapper = mapper;
        this.pushProcessingService = pushProcessingService;
    }
    
    private int sentRequests = 0;
    private LocalTime startTime = LocalTime.now();
    private LocalTime endTime = startTime.plusMinutes(1);

    public void handlePushRequest(String pushRequestString) {
        log.info("Push Request Received: " + pushRequestString);

        if (sentRequests == 0) {
            startTime = LocalTime.now();
            endTime = startTime.plusMinutes(1);
        }
        
        try {
            JsonNode pushRequestJson = mapper.readTree(pushRequestString);
            PushRequest pushRequest = mapper.treeToValue(pushRequestJson, PushRequest.class);
            log.debug("Successfully parsed Consumed Push Request: {}", pushRequest.toString());
            
            try {
                pushProcessingService.processPush(pushRequest);
                sentRequests++;
            } catch (Exception exception) {
                log.error("Unexpected Exception in PushProcessingService while processing Push Request: {}", pushRequest);
                log.error("Exception: {}", exception.toString());
            }
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("Error parsing kafka consumed message to JSON. Exception: \n {}", jsonProcessingException.toString());
        }

        if (LocalTime.now().isAfter(endTime)) {
            sentRequests = 0;
        }
        
        if (sentRequests >= 600) {
            try {
                log.debug("Rate Limit of this minute reached. Forcing thread to sleep for remaining " + 
                         (endTime.getSecond() - LocalTime.now().getSecond()) + " seconds");
                Thread.sleep((endTime.getSecond() - LocalTime.now().getSecond()) * 1000);
                sentRequests = 0;
            } catch (InterruptedException e) {
                log.error("Unexpected error while thread sleeping w.r.t Rate limiting: " + e.toString());
            }
        }
    }
}
