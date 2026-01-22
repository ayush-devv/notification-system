package com.notification.priority2_processor.service.exceptions;

import com.notification.priority2_processor.models.db.Notification;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

