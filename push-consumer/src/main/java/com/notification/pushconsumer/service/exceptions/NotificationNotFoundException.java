package com.notification.pushconsumer.service.exceptions;

/**
 * NotificationNotFoundException - Exception thrown when notification is not found in database
 */
public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
