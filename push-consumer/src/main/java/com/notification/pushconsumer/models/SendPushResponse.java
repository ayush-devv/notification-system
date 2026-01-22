package com.notification.pushconsumer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SendPushResponse - Response model for push notification sending operations
 * 
 * Fields:
 * - status: HTTP status code from push notification provider
 * - message: Response message or error description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendPushResponse {
    private int status;
    private String message;
}
