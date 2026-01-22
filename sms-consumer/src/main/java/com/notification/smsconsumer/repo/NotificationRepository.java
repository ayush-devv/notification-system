package com.notification.smsconsumer.repo;

import com.notification.smsconsumer.models.db.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * NotificationRepository - JPA repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findById(Long id);
}
