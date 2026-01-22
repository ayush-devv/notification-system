package com.notification.pushconsumer.service;

import com.notification.pushconsumer.models.PushRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * FailedNotificationsHandlerService - Handles failed push notification delivery attempts
 */
@Service
@Slf4j
public class FailedNotificationsHandlerService {
    
    public void handleFailedRequest(PushRequest pushRequest) {
        log.error("Failed to send push notification for Notification Id: {}. PushRequest: {}", 
                 pushRequest.getNotificationId(), pushRequest);
    }
}
