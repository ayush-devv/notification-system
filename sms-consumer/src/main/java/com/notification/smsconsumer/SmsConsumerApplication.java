package com.notification.smsconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmsConsumerApplication - Main entry point for SMS Consumer service
 * 
 * This service consumes SMS notifications from sms-topic with partition-level priority enforcement
 * and sends SMS via Twilio API.
 */
@SpringBootApplication
public class SmsConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmsConsumerApplication.class, args);
    }
}
