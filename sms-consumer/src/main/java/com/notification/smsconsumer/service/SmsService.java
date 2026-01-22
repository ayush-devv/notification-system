package com.notification.smsconsumer.service;

import com.notification.smsconsumer.models.SendSmsResponse;
import com.notification.smsconsumer.models.SmsRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SmsService - Twilio integration for actual SMS delivery
 * 
 * Responsibilities:
 * 1. Initializes Twilio SDK with account credentials
 * 2. Creates Twilio Message with from/to phone numbers and message content
 * 3. Makes API call to Twilio to send SMS
 * 4. Returns response with status code and message
 * 
 * Twilio Configuration:
 * - Account SID: Configured in application.properties (twilio.account.sid)
 * - Auth Token: Configured in application.properties (twilio.auth.token)
 * - From Number: Configured in application.properties (twilio.phone.number)
 * - Country Code: +91 (India) - hardcoded, can be made configurable
 * 
 * Response Status Codes:
 * - 200: SMS sent successfully
 * - 500: Exception occurred during Twilio API call
 */
@Service
@Slf4j
public class SmsService {
    
    @Value("${twilio.account.sid}")
    private String accountSid;
    
    @Value("${twilio.auth.token}")
    private String authToken;
    
    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    /**
     * Send SMS via Twilio API
     * 
     * @param smsRequest The SMS request containing mobile number and message
     * @return SendSmsResponse with status code and response message
     */
    public SendSmsResponse sendSms(SmsRequest smsRequest) {
        Twilio.init(accountSid, authToken);

        try {
            Message message = Message
                    .creator(
                            new PhoneNumber("+91" + smsRequest.getMobileNumber()),
                            new PhoneNumber(twilioPhoneNumber),
                            smsRequest.getMessage()
                    )
                    .create();

            log.info("Sms Request (Notification Id: {}). Response from Twilio: \n Status: {}, Body: {}, Twilio_Message: {}", 
                    smsRequest.getNotificationId(), message.getStatus(), message.getBody(), message.toString());
            
            return new SendSmsResponse(200, "Sid: " + message.getSid() + " Body: " + message.getBody());
        } catch (Exception exception) {
            log.error("Something went wrong with Twilio. Exception: {}", exception.toString());
            return new SendSmsResponse(500, "Exception occurred in Sms Service-Twilio");
        }
    }
}
