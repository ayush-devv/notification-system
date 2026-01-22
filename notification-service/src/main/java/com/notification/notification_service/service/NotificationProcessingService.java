package com.notification.notification_service.service;

import com.notification.notification_service.dto.Content;
import com.notification.notification_service.dto.NotificationRequest;
import com.notification.notification_service.dto.Recipient;
import com.notification.notification_service.entity.Template;
import com.notification.notification_service.repository.TemplateRepository;
import com.notification.notification_service.service.exceptions.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Service
@Slf4j
public class NotificationProcessingService {
    
    // STEP 1: Declare RedisService and TemplateRepository dependencies
    private final RedisService redisService;
    private final TemplateRepository templateRepository;

    
    
    // STEP 2: Create constructor with RedisService and TemplateRepository parameters
    public NotificationProcessingService(RedisService redisService, TemplateRepository templateRepository) {
        this.redisService = redisService;
        this.templateRepository = templateRepository;
    }
    
    
    // STEP 3: Implement public void validateRequest(NotificationRequest notificationRequest)
    public void validateRequest(NotificationRequest notificationRequest) {
        // Validate priority
        int priority = notificationRequest.getNotificationPriority();
        if (priority != -1 && priority != 1 && priority != 2 && priority != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority value");
        }

        // Validate channels
        String[] validChannels = {"email", "sms", "push"};
        for (String channel : notificationRequest.getChannels()) {
            if (!Arrays.asList(validChannels).contains(channel.toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid channel: " + channel);
            }
        }

        // Validate userId
        if (notificationRequest.getRecipient().getUserId() == null || notificationRequest.getRecipient().getUserId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId cannot be empty");
        }

        // Validate message or template
        Content content = notificationRequest.getContent();
        if ((content.getMessage() == null || content.getMessage().isEmpty()) &&
                (content.getTemplateName() == null || content.getTemplateName().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either message or templateName must be provided");
        }
    }
    
    
    // STEP 4: Implement public void assignPriority(NotificationRequest notificationRequest)
    public void assignPriority(NotificationRequest notificationRequest) {
        if (notificationRequest.getNotificationPriority() != -1) {
            // Priority already set, no need to assign
            return;
        }

        Content content = notificationRequest.getContent();
        String templateName = content.getTemplateName();

        if (templateName != null && !templateName.isEmpty()) {
            // Using template - fetch priority from Redis or DB
            int redisPriority = redisService.get(templateName);
            if (redisPriority != -1) {
                // Found in Redis cache
                notificationRequest.setNotificationPriority(redisPriority);
                return;
            }

            // Not found in Redis, check database
            try {
                Template template = templateRepository.findByName(templateName)
                        .orElseThrow(() -> new TemplateNotFoundException("Template with name: " + templateName + " Not found"));
                
                int templatePriority = template.getTemplatePriority();
                // Cache in Redis for future requests
                if (templatePriority == 1 || templatePriority == 2 || templatePriority == 3) {
                    redisService.set(templateName, templatePriority);
                }
                notificationRequest.setNotificationPriority(templatePriority);
            } catch (TemplateNotFoundException e) {
                log.error("{} For notificationRequest: {}", e, notificationRequest);
                // Default to medium priority if template not found
                notificationRequest.setNotificationPriority(2);
            } catch (Exception e) {
                log.error("Unexpected Exception: {} For notificationRequest: {}", e, notificationRequest);
                notificationRequest.setNotificationPriority(2);
            }
        } else {
            // No template, assign default medium priority
            notificationRequest.setNotificationPriority(2);
        }
    }

    
}
