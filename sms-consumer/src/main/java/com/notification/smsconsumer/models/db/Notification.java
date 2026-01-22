package com.notification.smsconsumer.models.db;

import com.notification.smsconsumer.models.enums.Channel;
import com.notification.smsconsumer.models.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Notification Entity - Represents a notification in the system
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private Channel channel;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    private String message;
    private String hash;
}
