package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import com.indra.notifications.sender.EmailSender;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationAuditLog auditLog;
    
    private final EmailSender emailSender;

    NotificationService(NotificationAuditLog auditLog, EmailSender emailSender) {
        this.auditLog = auditLog;
        this.emailSender = emailSender;
    }

    public void notify(String to, String subject, String body) {
        
        emailSender.send(to, subject, body);

        auditLog.record(to, subject);
    }
}
