package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import com.indra.notifications.config.NotificationProperties;
import com.indra.notifications.email.EmailSender;
import com.indra.notifications.email.TransientEmailException;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailSender emailSender;
    private final NotificationAuditLog auditLog;
    private final NotificationProperties properties;

    public NotificationService(EmailSender emailSender, NotificationAuditLog auditLog,
            NotificationProperties properties) {
        this.emailSender = emailSender;
        this.auditLog = auditLog;
        this.properties = properties;
    }

    public void notify(String to, String subject, String body) {
        int retriesRemaining = properties.getRetryAttempts();
        while (true) {
            try {
                emailSender.send(to, subject, body);
                break;
            } catch (TransientEmailException exception) {
                if (retriesRemaining-- <= 0) {
                    throw exception;
                }
            }
        }
        auditLog.record(to, subject);
    }
}
