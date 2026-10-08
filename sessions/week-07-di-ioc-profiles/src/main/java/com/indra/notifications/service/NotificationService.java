package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailSender emailSender;
    private final NotificationAuditLog auditLog;

    public NotificationService(EmailSender emailSender, NotificationAuditLog auditLog) {
        this.emailSender = emailSender;
        this.auditLog = auditLog;
    }

    public void notify(String to, String subject, String body) {
        emailSender.send(to, subject, body);
        auditLog.record(to, subject);
    }

    public EmailSender getEmailSender() {
        return emailSender;
    }
}
