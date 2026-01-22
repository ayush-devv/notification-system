package com.notification.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipient {
    @NotBlank(message = "User ID cannot be empty")
    private String userId;
    
    private String userEmail;
}
