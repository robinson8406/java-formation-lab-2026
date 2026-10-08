package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import com.indra.notifications.email.EmailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailSender emailSender;
    private final ObjectProvider<EmailSender> fallbackEmailSender;
    private final NotificationAuditLog auditLog;
    private final int retryAttempts;

    public NotificationService(@Qualifier("primaryEmailSender") EmailSender emailSender,
                               @Qualifier("secondaryEmailSender") ObjectProvider<EmailSender> fallbackEmailSender,
                               NotificationAuditLog auditLog,
                               @Value("${notification.retry-attempts:1}") int retryAttempts) {
        this.emailSender = emailSender;
        this.fallbackEmailSender = fallbackEmailSender;
        this.auditLog = auditLog;
        this.retryAttempts = retryAttempts;
    }

    public void notify(String to, String subject, String body) {
        if (trySend(emailSender, to, subject, body)) {
            auditLog.record(to, subject);
            return;
        }

        EmailSender fallback = fallbackEmailSender.getIfAvailable();
        if (fallback != null && trySend(fallback, to, subject, body)) {
            auditLog.record(to, subject);
            return;
        }

        throw new IllegalStateException(
                "No se pudo enviar la notificación a " + to + " tras agotar canal principal"
                        + (fallback != null ? " y canal de respaldo" : " (sin canal de respaldo configurado)"));
    }

    private boolean trySend(EmailSender sender, String to, String subject, String body) {
        int attempts = 0;
        while (attempts < retryAttempts) {
            try {
                sender.send(to, subject, body);
                return true;
            } catch (RuntimeException ex) {
                attempts++;
            }
        }
        return false;
    }

    int getRetryAttempts() {
        return retryAttempts;
    }
}