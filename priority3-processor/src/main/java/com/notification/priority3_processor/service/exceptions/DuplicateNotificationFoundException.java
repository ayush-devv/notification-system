package com.notification.priority3_processor.service.exceptions;

import com.notification.priority3_processor.models.db.Notification;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

