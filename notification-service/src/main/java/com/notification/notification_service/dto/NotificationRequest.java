package com.notification.notification_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private int notificationPriority;  // -1 (auto-assign), 1 (high), 2 (medium), 3 (low)
    
    @NotNull(message = "Channels cannot be null")
    private String[] channels;         // ["email", "sms", "push"]
    
    @NotNull(message = "Recipient cannot be null")
    @Valid
    private Recipient recipient;
    
    @NotNull(message = "Content cannot be null")
    @Valid
    private Content content;
}
