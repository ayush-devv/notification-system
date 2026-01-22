package com.notification.smsconsumer.models.db;

import com.notification.smsconsumer.models.enums.Channel;
import com.notification.smsconsumer.models.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeliveryLog Entity - Tracks delivery status for notifications
 */
@Entity
@Table(name = "delivery_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;
    
    @Enumerated(EnumType.STRING)
    private Channel channel;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    private String errorMessage;
    
    public DeliveryLog(Notification notification, Channel channel, Status status, String errorMessage) {
        this.notification = notification;
        this.channel = channel;
        this.status = status;
        this.errorMessage = errorMessage;
    }
}
