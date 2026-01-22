package com.notification.pushconsumer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PushRequest - Request model for push notification sending
 * 
 * Fields:
 * - title: Notification title
 * - message: Notification message content
 * - action: Action to perform when notification is tapped
 * - notificationId: Associated notification ID for tracking status across microservices
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushRequest {
    private String title;
    private String message;
    private String action;
    private Long notificationId;

    public PushRequest(String title, String message, String action) {
        this.title = title;
        this.message = message;
        this.action = action;
    }
}
