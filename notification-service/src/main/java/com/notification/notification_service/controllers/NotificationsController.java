package com.notification.notification_service.controllers;

import com.notification.notification_service.dto.NotificationRequest;
import com.notification.notification_service.service.KafkaService;
import com.notification.notification_service.service.RedisService;
import com.notification.notification_service.service.NotificationProcessingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api")
public class NotificationsController {

    // Dependencies (injected by Spring)
    private final KafkaService kafkaService;
    private final RedisService redisService;
    private final NotificationProcessingService notificationProcessingService;

    // Constructor (Spring injects dependencies here)
    public NotificationsController(
            KafkaService kafkaService,
            RedisService redisService,
            NotificationProcessingService notificationProcessingService
    ) {
        this.kafkaService = kafkaService;
        this.redisService = redisService;
        this.notificationProcessingService = notificationProcessingService;
    }

    // Health Check Endpoint
    // URL: GET /api/health
    @GetMapping("/health")
    public String getHealth() {
        return "Running";
    }

    // Send Notification Endpoint
    // URL: POST /api/send-notification
    // Input: NotificationRequest (from JSON body)
    @PostMapping("/send-notification")
    public ResponseEntity<?> sendNotification(@Valid @RequestBody NotificationRequest notificationRequest) {
        try {
            // STEP 1: Validate the incoming request
            // Checks:
            //   - Priority is valid (-1, 1, 2, or 3)
            //   - Channels are valid (email, sms, push)
            //   - userId is not empty
            //   - Has message OR using template
            notificationProcessingService.validateRequest(notificationRequest);

            // STEP 2: Assign priority if not provided
            // Priority -1 means no priority, need to auto-assign
            if (notificationRequest.getNotificationPriority() == -1) {
                // This will:
                //   - Check if using template
                //   - If yes: lookup priority from Redis/DB
                //   - If no: default to priority 2 (medium)
                notificationProcessingService.assignPriority(notificationRequest);
            }

            // STEP 3: Forward to Kafka
            // Routes to appropriate topic:
            //   - Priority 1 → "priority-1" topic
            //   - Priority 2 → "priority-2" topic
            //   - Priority 3 → "priority-3" topic
            kafkaService.sendNotification(notificationRequest);

            // STEP 4: Log success
            log.debug("Notification forwarded to Kafka with priority: {}", notificationRequest.getNotificationPriority());

            // STEP 5: Return success response (HTTP 202 ACCEPTED)
            return ResponseEntity.accepted().body("Notification accepted for processing.");

        } catch (InvalidRequestException e) {
            // Request validation failed
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseStatusException e) {
            // Business logic validation failed
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (KafkaException e) {
            // Failed to send to Kafka
            log.error("Failed to forward notification to Kafka: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing notification.");

        } catch (Exception e) {
            // Unexpected error
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}