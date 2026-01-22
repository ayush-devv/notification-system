package com.notification.emailconsumer.service;

import com.notification.emailconsumer.models.EmailRequest;
import com.notification.emailconsumer.models.SendEmailResponse;
import com.notification.emailconsumer.models.db.DeliveryLog;
import com.notification.emailconsumer.models.db.Notification;
import com.notification.emailconsumer.models.enums.Channel;
import com.notification.emailconsumer.models.enums.Status;
import com.notification.emailconsumer.repo.DeliveryLogRepository;
import com.notification.emailconsumer.repo.NotificationRepository;
import com.notification.emailconsumer.service.exceptions.NotificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * EmailProcessingService - Orchestrates the email sending workflow
 * 
 * Responsibilities:
 * 1. Sends emails to third-party vendors via EmailService
 * 2. Updates notification status in database upon successful send
 * 3. Creates delivery logs for tracking
 * 4. Delegates failed requests to FailedNotificationsHandlerService
 * 
 * Flow:
 * 1. Receive EmailRequest from MessageHandlerService (after rate limiting)
 * 2. Call EmailService to send via SendGrid
 * 3. If status 200-299 (success):
 *    - Update notification status to 'sent' in database
 *    - Create delivery log with 'sent' status
 * 4. If status >= 300 (failure):
 *    - Delegate to FailedNotificationsHandlerService for retry/logging
 */
@Service
@Slf4j
public class EmailProcessingService {
    EmailService emailService;
    DeliveryLogRepository deliveryLogRepository;
    NotificationRepository notificationRepository;
    FailedNotificationsHandlerService failedNotificationsHandlerService;
    
    public EmailProcessingService(EmailService emailService, 
                                 DeliveryLogRepository deliveryLogRepository, 
                                 NotificationRepository notificationRepository, 
                                 FailedNotificationsHandlerService failedNotificationsHandlerService) {
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.failedNotificationsHandlerService = failedNotificationsHandlerService;
    }

    /**
     * Process email request by sending to vendors and updating database
     * 
     * @param emailRequest The email request containing recipient, subject, message, attachments
     */
    public void processEmail(EmailRequest emailRequest) {
        SendEmailResponse response = sendEmailToVendors(emailRequest);

        try {
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                // Email sent successfully - update database
                Notification notification = notificationRepository.findById(emailRequest.getNotificationId())
                        .orElseThrow(() -> {
                            log.error("Notification with Id: " + emailRequest.getNotificationId() + " Not found");
                            return new NotificationNotFoundException("Notification with Id: " + emailRequest.getNotificationId() + " Not found");
                        });
                
                notification.setStatus(Status.sent);
                
                try {
                    notificationRepository.save(notification); // Update status
                    log.info("Status updated to SENT for Notification Id: " + emailRequest.getNotificationId());
                } catch (Exception exception) {
                    log.error("Exception while updating status of Notification for emailRequest: {}", emailRequest);
                    log.error("Exception: {}", exception.toString());
                }
                
                try {
                    deliveryLogRepository.save(new DeliveryLog(notification, Channel.email, Status.sent, ""));
                } catch (Exception exception) {
                    log.error("Exception while creating delivery log for Notification id {} for emailRequest: {}", 
                             notification.getId(), emailRequest);
                    log.error("Exception: {}", exception.toString());
                }
            } else {
                // Email send failed - delegate to failure handler
                failedNotificationsHandlerService.handleFailedRequest(emailRequest);
            }
        } catch (NotificationNotFoundException exception) {
            log.error("Notification with Id: " + emailRequest.getNotificationId() + 
                     " Not found while trying to update notification status/creating delivery log");
        }
    }

    /**
     * Send email to third-party vendor (SendGrid) and return response
     * 
     * @param emailRequest The email request to send
     * @return SendEmailResponse with status code and message
     */
    private SendEmailResponse sendEmailToVendors(EmailRequest emailRequest) {
        SendEmailResponse sendEmailResponse = emailService.sendEmail(emailRequest);

        if (sendEmailResponse.getStatus() >= 200 && sendEmailResponse.getStatus() < 300) {
            sendEmailResponse.setMessage("Email Sent");
            log.info("EmailRequest {} sent successfully", emailRequest.toString());
            return sendEmailResponse;
        } else {
            log.error("Failed to send EmailRequest {}. Message: {}", 
                     emailRequest.toString(), sendEmailResponse.getMessage());
            sendEmailResponse.setMessage("Something went wrong with SendGrid.");
            return sendEmailResponse;
        }
    }
}
