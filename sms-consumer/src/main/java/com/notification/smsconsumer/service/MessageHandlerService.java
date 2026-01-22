package com.notification.smsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.smsconsumer.models.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * MessageHandlerService - Handles rate limiting and JSON deserialization for SMS requests
 * 
 * Responsibilities:
 * 1. Rate limiting: Enforces 600 SMS/minute limit (Twilio rate limit)
 * 2. JSON deserialization: Parses Kafka message string to SmsRequest object
 * 3. Delegates to SmsProcessingService for actual SMS sending
 * 
 * Rate Limiting Logic:
 * - Tracks sentRequests counter
 * - Resets counter every minute (startTime to endTime)
 * - When 600 limit reached, sleeps thread until next minute
 * - Prevents exceeding third-party SMS provider rate limits
 */
@Service
@Slf4j
public class MessageHandlerService {
    ObjectMapper mapper;
    SmsProcessingService smsProcessingService;

    public MessageHandlerService(ObjectMapper mapper, SmsProcessingService smsProcessingService) {
        this.mapper = mapper;
        this.smsProcessingService = smsProcessingService;
    }
    
    private int sentRequests = 0;
    private LocalTime startTime = LocalTime.now();
    private LocalTime endTime = startTime.plusMinutes(1);

    /**
     * Handle SMS request from Kafka consumer
     * Applies rate limiting and delegates to processing service
     * 
     * @param smsRequestString JSON string from Kafka message
     */
    public void handleSmsRequest(String smsRequestString) {
        log.info("SMS Request Received: " + smsRequestString);

        // Initialize rate limiting window
        if (sentRequests == 0) {
            startTime = LocalTime.now();
            endTime = startTime.plusMinutes(1);
        }
        
        try {
            // Deserialize JSON to SmsRequest object
            JsonNode smsRequestJson = mapper.readTree(smsRequestString);
            SmsRequest smsRequest = mapper.treeToValue(smsRequestJson, SmsRequest.class);
            log.debug("Successfully parsed Consumed Sms Request: {}", smsRequest.toString());
            
            try {
                smsProcessingService.processSms(smsRequest);
                sentRequests++;
            } catch (Exception exception) {
                log.error("Unexpected Exception in SmsProcessingService while processing Sms Request: {}", smsRequest);
                log.error("Exception: {}", exception.toString());
            }
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("Error parsing kafka consumed message to JSON. Exception: \n {}", jsonProcessingException.toString());
        }

        // Reset counter if minute elapsed
        if (LocalTime.now().isAfter(endTime)) {
            sentRequests = 0;
        }
        
        // Rate limiting: Sleep if 600 SMS/minute limit reached
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
