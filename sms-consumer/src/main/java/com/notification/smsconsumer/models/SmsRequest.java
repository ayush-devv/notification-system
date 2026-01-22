package com.notification.smsconsumer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SmsRequest - Request model for SMS sending
 * 
 * Fields:
 * - mobileNumber: Recipient's phone number
 * - message: SMS content to send
 * - notificationId: Associated notification ID for tracking status across microservices
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    private String mobileNumber;
    private String message;
    private Long notificationId;

    public SmsRequest(String mobileNumber, String message) {
        this.mobileNumber = mobileNumber;
        this.message = message;
    }
}
