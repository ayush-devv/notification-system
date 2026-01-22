package com.notification.smsconsumer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SendSmsResponse - Response model for SMS sending operations
 * 
 * Fields:
 * - status: HTTP status code from SMS provider
 * - message: Response message or error description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendSmsResponse {
    private int status;
    private String message;
}
