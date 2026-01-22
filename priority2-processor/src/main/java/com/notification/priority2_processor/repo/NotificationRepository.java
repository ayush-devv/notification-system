package com.notification.priority2_processor.repo;

import com.notification.priority2_processor.models.db.Notification;
import com.notification.priority2_processor.models.enums.Channel;
import com.notification.priority2_processor.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Find notifications for a specific user
    List<Notification> findByUserId(Long userId);

    // Find notifications by user and channel
    List<Notification> findByUserIdAndChannel(Long userId, Channel channel);

    // Find notifications by status
    List<Notification> findByStatus(Status status);
}

