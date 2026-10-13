package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class NotificationService {

    private final EmailSender emailSender;
    private final NotificationAuditLog auditLog;

    public NotificationService(EmailSender emailSender, NotificationAuditLog auditLog) {
        this.emailSender = Objects.requireNonNull(emailSender);
        this.auditLog = Objects.requireNonNull(auditLog);
    }

    public void notify(String to, String subject, String body) {
        emailSender.send(to, subject, body);
        auditLog.record(to, subject);
    }
}
