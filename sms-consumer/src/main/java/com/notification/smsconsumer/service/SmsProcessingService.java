package com.notification.smsconsumer.service;

import com.notification.smsconsumer.models.SendSmsResponse;
import com.notification.smsconsumer.models.SmsRequest;
import com.notification.smsconsumer.models.db.DeliveryLog;
import com.notification.smsconsumer.models.db.Notification;
import com.notification.smsconsumer.models.enums.Channel;
import com.notification.smsconsumer.models.enums.Status;
import com.notification.smsconsumer.repo.DeliveryLogRepository;
import com.notification.smsconsumer.repo.NotificationRepository;
import com.notification.smsconsumer.service.exceptions.NotificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SmsProcessingService - Orchestrates the SMS sending workflow
 * 
 * Responsibilities:
 * 1. Sends SMS to third-party vendors via SmsService
 * 2. Updates notification status in database upon successful send
 * 3. Creates delivery logs for tracking
 * 4. Delegates failed requests to FailedNotificationsHandlerService
 * 
 * Flow:
 * 1. Receive SmsRequest from MessageHandlerService (after rate limiting)
 * 2. Call SmsService to send via Twilio
 * 3. If status 200-299 (success):
 *    - Update notification status to 'sent' in database
 *    - Create delivery log with 'sent' status
 * 4. If status >= 300 (failure):
 *    - Delegate to FailedNotificationsHandlerService for retry/logging
 */
@Service
@Slf4j
public class SmsProcessingService {
    SmsService smsService;
    DeliveryLogRepository deliveryLogRepository;
    NotificationRepository notificationRepository;
    FailedNotificationsHandlerService failedNotificationsHandlerService;
    
    public SmsProcessingService(SmsService smsService, 
                               FailedNotificationsHandlerService failedNotificationsHandlerService,
                               DeliveryLogRepository deliveryLogRepository, 
                               NotificationRepository notificationRepository) {
        this.smsService = smsService;
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.failedNotificationsHandlerService = failedNotificationsHandlerService;
    }

    /**
     * Process SMS request by sending to vendors and updating database
     * 
     * @param smsRequest The SMS request containing mobile number, message, notificationId
     */
    public void processSms(SmsRequest smsRequest) {
        SendSmsResponse response = sendSmsToVendors(smsRequest);

        try {
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                // SMS sent successfully - update database
                Notification notification = notificationRepository.findById(smsRequest.getNotificationId())
                        .orElseThrow(() -> {
                            log.error("Notification with Id: " + smsRequest.getNotificationId() + " Not found");
                            return new NotificationNotFoundException("Notification with Id: " + smsRequest.getNotificationId() + " Not found");
                        });
                
                notification.setStatus(Status.sent);
                
                try {
                    notificationRepository.save(notification); // Update status
                    log.info("Status updated to SENT for Notification Id: " + smsRequest.getNotificationId());
                } catch (Exception exception) {
                    log.error("Exception while updating status of Notification for smsRequest: {}", smsRequest);
                    log.error("Exception: {}", exception.toString());
                }
                
                try {
                    deliveryLogRepository.save(new DeliveryLog(notification, Channel.sms, Status.sent, ""));
                } catch (Exception exception) {
                    log.error("Exception while creating delivery log for Notification id {} for smsRequest: {}", 
                             notification.getId(), smsRequest);
                    log.error("Exception: {}", exception.toString());
                }
            } else {
                // SMS send failed - delegate to failure handler
                failedNotificationsHandlerService.handleFailedRequest(smsRequest);
            }
        } catch (NotificationNotFoundException exception) {
            log.error("Notification with Id: " + smsRequest.getNotificationId() + 
                     " Not found while trying to update notification status/creating delivery log");
        }
    }

    /**
     * Send SMS to third-party vendor (Twilio) and return response
     * 
     * @param smsRequest The SMS request to send
     * @return SendSmsResponse with status code and message
     */
    private SendSmsResponse sendSmsToVendors(SmsRequest smsRequest) {
        SendSmsResponse sendSmsResponse = smsService.sendSms(smsRequest);

        if (sendSmsResponse.getStatus() >= 200 && sendSmsResponse.getStatus() < 300) {
            sendSmsResponse.setMessage("Sms Sent");
            log.info("SmsRequest {} sent successfully", smsRequest.toString());
            return sendSmsResponse;
        } else {
            log.error("Failed to send SmsRequest {}. Message: {}", 
                     smsRequest.toString(), sendSmsResponse.getMessage());
            sendSmsResponse.setMessage("Something went wrong with Twilio.");
            return sendSmsResponse;
        }
    }
}
