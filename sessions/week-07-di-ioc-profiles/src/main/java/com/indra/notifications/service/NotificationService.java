package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationAuditLog auditLog;
    private final EmailSender emailSender;

    public NotificationService(NotificationAuditLog auditLog, EmailSender emailSender) {
        this.auditLog = auditLog;
        this.emailSender = emailSender;
    }

    public void notify(String to, String subject, String body) {
        emailSender.send(to, subject, body);
        auditLog.record(to, subject);
    }
}
