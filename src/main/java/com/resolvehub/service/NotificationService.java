package com.resolvehub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Email notification service for incident lifecycle events.
 *
 * <p>Uses Spring Mail to send plain-text emails. Each notification type
 * has its own template method for clarity and easy future extension to
 * HTML templates (e.g. via Thymeleaf).</p>
 *
 * <p>All methods are designed to be called asynchronously from
 * {@link com.resolvehub.event.IncidentEventListener}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@resolvehub.io}")
    private String fromAddress;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    public void sendAssignmentNotification(String toEmail, String toName,
                                            UUID incidentId, String title, String assignedBy) {
        String subject = "[ResolveHub] Case assigned to you: " + title;
        String body = """
                Hi %s,

                A case has been assigned to you by %s.

                  Case: %s
                  Link: %s/cases/%s

                Please review and take action at your earliest convenience.

                — ResolveHub Notifications
                """.formatted(toName, assignedBy, title, baseUrl, incidentId);

        send(toEmail, subject, body);
    }

    public void sendStatusChangeNotification(String toEmail, String toName,
                                              UUID incidentId, String title, String detail) {
        String subject = "[ResolveHub] Case status updated: " + title;
        String body = """
                Hi %s,

                The status of your case has been updated.

                  Case: %s
                  Change: %s
                  Link: %s/cases/%s

                — ResolveHub Notifications
                """.formatted(toName, title, detail, baseUrl, incidentId);

        send(toEmail, subject, body);
    }

    public void sendCommentNotification(String toEmail, String toName,
                                         UUID incidentId, String title, String commenterName) {
        String subject = "[ResolveHub] New comment on: " + title;
        String body = """
                Hi %s,

                %s left a comment on case "%s".

                  Link: %s/cases/%s

                — ResolveHub Notifications
                """.formatted(toName, commenterName, title, baseUrl, incidentId);

        send(toEmail, subject, body);
    }

    public void sendSlaBreachNotification(String toEmail, String toName,
                                           UUID incidentId, String title, String detail) {
        String subject = "[ResolveHub] SLA BREACH: " + title;
        String body = """
                Hi %s,

                A case has breached its SLA deadline and requires immediate attention.

                  Case: %s
                  Detail: %s
                  Link: %s/cases/%s

                Please take action immediately.

                — ResolveHub Notifications
                """.formatted(toName, title, detail, baseUrl, incidentId);

        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
