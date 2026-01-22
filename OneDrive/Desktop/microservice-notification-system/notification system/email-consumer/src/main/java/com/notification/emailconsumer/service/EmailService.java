package com.notification.emailconsumer.service;

import com.notification.emailconsumer.models.EmailRequest;
import com.notification.emailconsumer.models.SendEmailResponse;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * EmailService - SendGrid integration for actual email delivery
 * 
 * Responsibilities:
 * 1. Builds SendGrid Mail object with from/to/subject/content
 * 2. Handles email attachments (if provided)
 * 3. Makes HTTP POST request to SendGrid API
 * 4. Returns response with status code and body
 * 
 * SendGrid Configuration:
 * - API Key: Configured in application.properties (sendgrid.api.key)
 * - Verified Sender: Configured in application.properties (sendgrid.sender.email)
 * - Endpoint: mail/send
 * - Method: POST
 * 
 * Response Status Codes:
 * - 200-299: Email sent successfully
 * - 400+: Email send failed (invalid request, auth error, etc.)
 * - 500: IOException occurred during SendGrid API call
 */
@Service
@Slf4j
public class EmailService {
    
    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;
    
    @Value("${sendgrid.sender.email}")
    private String senderEmail;

    /**
     * Send email via SendGrid API
     * 
     * @param emailRequest The email request containing recipient, subject, message, attachments
     * @return SendEmailResponse with status code and response body
     */
    public SendEmailResponse sendEmail(EmailRequest emailRequest) {
        Email from = new Email(senderEmail);
        String subject = emailRequest.getEmailSubject() + " | Scalable Notification System";
        Email to = new Email(emailRequest.getEmailId());
        Content content = new Content("text/plain", emailRequest.getMessage());
        Mail mail = new Mail(from, subject, to, content);

        // Add attachments if provided
        if (emailRequest.getEmailAttachments().length != 0) {
            // TODO: Implement attachment handling
            // For now, adding sample attachment
            Attachments attachments2 = new Attachments();
            attachments2.setContent("BwdW");
            attachments2.setType("image/png");
            attachments2.setFilename("banner.png");
            attachments2.setDisposition("inline");
            attachments2.setContentId("Banner");
            mail.addAttachments(attachments2);
            mail.addAttachments(attachments2);
        }

        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            log.info("Email Request (Notification Id: {}). Response from SendGrid: \n Status Code: {}, Body: {}, Headers: {}", 
                    emailRequest.getNotificationId(), response.getStatusCode(), 
                    response.getBody(), response.getHeaders());
            
            return new SendEmailResponse(response.getStatusCode(), response.getBody());
        } catch (IOException ex) {
            log.error("Something went wrong with SendGrid. Exception: {}", ex.toString());
            return new SendEmailResponse(500, "IO Exception occurred in Email Service-SendGrid");
        }
    }
}
