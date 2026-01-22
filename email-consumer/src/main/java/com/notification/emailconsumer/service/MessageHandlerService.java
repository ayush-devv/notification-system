package com.notification.emailconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.emailconsumer.models.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * Message Handler Service - Rate Limiting Layer
 * 
 * RESPONSIBILITIES:
 * 1. Deserialize Kafka messages to EmailRequest objects
 * 2. Implement rate limiting (600 emails/minute for SendGrid)
 * 3. Delegate to EmailProcessingService for actual sending
 * 
 * RATE LIMITING LOGIC:
 * - Tracks sentRequests counter
 * - Resets every minute
 * - If limit reached (600), sleeps until next minute
 * - Prevents SendGrid API throttling
 */
@Service
@Slf4j
public class MessageHandlerService {
    ObjectMapper mapper;
    EmailProcessingService emailProcessingService;

    public MessageHandlerService(ObjectMapper mapper, EmailProcessingService emailProcessingService){
        this.mapper = mapper;
        this.emailProcessingService = emailProcessingService;
    }
    private int sentRequests = 0;
    private LocalTime startTime = LocalTime.now();
    private LocalTime endTime = startTime.plusMinutes(1);

    public void handleEmailRequest(String emailRequestString){
        log.info("Email Request Received: "+emailRequestString);

        if(sentRequests == 0){ //Rate limiting configuration as per third party limits - (this case, 600/min)
            startTime = LocalTime.now();
            endTime = startTime.plusMinutes(1);
        }
        try{
            JsonNode emailRequestJson = mapper.readTree(emailRequestString);
            EmailRequest emailRequest = mapper.treeToValue(emailRequestJson,EmailRequest.class);
            log.debug("Successfully parsed Consumed Email Request: {}", emailRequest.toString());
            try{
                emailProcessingService.processEmail(emailRequest);
                sentRequests++;
            } catch (Exception exception){
                log.error("Unexpected Exception in EmailProcessingService while processing Email Request: {}", emailRequest);
                log.error("Exception: {}", exception.toString());
            }
        } catch (JsonProcessingException jsonProcessingException){
            log.error("Error parsing kafka consumed message to JSON. Exception: \n {}", jsonProcessingException.toString());
        }

        //Achieving rate limiting for consumer
        if(LocalTime.now().isAfter(endTime)){
            sentRequests = 0;
        }
        if(sentRequests >= 600){
            try {
                log.debug("Rate Limit of this minute reached. Forcing thread to sleep for remaining "+(endTime.getSecond() - LocalTime.now().getSecond())+" seconds");
                Thread.sleep((endTime.getSecond() - LocalTime.now().getSecond()) * 1000L);
                sentRequests = 0;
            } catch (InterruptedException e) {
                log.error("Unexpected error while thread sleeping w.r.t Rate limiting: "+e.toString());
            }
        }

    }
}
