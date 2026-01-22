package com.notification.smsconsumer.repo;

import com.notification.smsconsumer.models.db.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * DeliveryLogRepository - JPA repository for DeliveryLog entity
 */
@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
}
