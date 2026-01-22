package com.notification.notification_service.repository;

import com.notification.notification_service.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    Optional<Template> findByName(String name);
}
