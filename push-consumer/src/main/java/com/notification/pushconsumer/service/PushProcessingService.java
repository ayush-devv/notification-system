package com.notification.pushconsumer.service;

import com.notification.pushconsumer.models.PushRequest;
import com.notification.pushconsumer.models.SendPushResponse;
import com.notification.pushconsumer.models.db.DeliveryLog;
import com.notification.pushconsumer.models.db.Notification;
import com.notification.pushconsumer.models.enums.Channel;
import com.notification.pushconsumer.models.enums.Status;
import com.notification.pushconsumer.repo.DeliveryLogRepository;
import com.notification.pushconsumer.repo.NotificationRepository;
import com.notification.pushconsumer.service.exceptions.NotificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * PushProcessingService - Orchestrates the push notification sending workflow
 */
@Service
@Slf4j
public class PushProcessingService {
    PushService pushService;
    DeliveryLogRepository deliveryLogRepository;
    NotificationRepository notificationRepository;
    FailedNotificationsHandlerService failedNotificationsHandlerService;
    
    public PushProcessingService(PushService pushService, 
                                FailedNotificationsHandlerService failedNotificationsHandlerService,
                                DeliveryLogRepository deliveryLogRepository, 
                                NotificationRepository notificationRepository) {
        this.pushService = pushService;
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.failedNotificationsHandlerService = failedNotificationsHandlerService;
    }

    public void processPush(PushRequest pushRequest) {
        SendPushResponse response = sendPushToVendors(pushRequest);

        try {
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                Notification notification = notificationRepository.findById(pushRequest.getNotificationId())
                        .orElseThrow(() -> {
                            log.error("Notification with Id: " + pushRequest.getNotificationId() + " Not found");
                            return new NotificationNotFoundException("Notification with Id: " + pushRequest.getNotificationId() + " Not found");
                        });
                
                notification.setStatus(Status.sent);
                
                try {
                    notificationRepository.save(notification);
                    log.info("Status updated to SENT for Notification Id: " + pushRequest.getNotificationId());
                } catch (Exception exception) {
                    log.error("Exception while updating status of Notification for pushRequest: {}", pushRequest);
                    log.error("Exception: {}", exception.toString());
                }
                
                try {
                    deliveryLogRepository.save(new DeliveryLog(notification, Channel.push, Status.sent, ""));
                } catch (Exception exception) {
                    log.error("Exception while creating delivery log for Notification id {} for pushRequest: {}", 
                             notification.getId(), pushRequest);
                    log.error("Exception: {}", exception.toString());
                }
            } else {
                failedNotificationsHandlerService.handleFailedRequest(pushRequest);
            }
        } catch (NotificationNotFoundException exception) {
            log.error("Notification with Id: " + pushRequest.getNotificationId() + 
                     " Not found while trying to update notification status/creating delivery log");
        }
    }

    private SendPushResponse sendPushToVendors(PushRequest pushRequest) {
        SendPushResponse sendPushResponse = pushService.sendPushNotification(pushRequest);

        if (sendPushResponse.getStatus() >= 200 && sendPushResponse.getStatus() < 300) {
            sendPushResponse.setMessage("Push Notification Sent");
            log.info("PushRequest {} sent successfully", pushRequest.toString());
            return sendPushResponse;
        } else {
            log.error("Failed to send PushRequest {}. Message: {}", 
                     pushRequest.toString(), sendPushResponse.getMessage());
            sendPushResponse.setMessage("Something went wrong with Push Service.");
            return sendPushResponse;
        }
    }
}
