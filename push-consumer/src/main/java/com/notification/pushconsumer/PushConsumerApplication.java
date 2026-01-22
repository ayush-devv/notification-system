package com.notification.pushconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PushConsumerApplication - Main entry point for Push Notification Consumer service
 * 
 * This service consumes push notifications from push-topic with partition-level priority enforcement
 * and sends push notifications via FCM (Firebase Cloud Messaging).
 */
@SpringBootApplication
public class PushConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PushConsumerApplication.class, args);
    }
}
