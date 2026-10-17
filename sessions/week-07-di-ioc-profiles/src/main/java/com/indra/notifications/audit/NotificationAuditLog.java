package com.indra.notifications.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class NotificationAuditLog {

    private static final Logger log = LoggerFactory.getLogger(NotificationAuditLog.class);

    public void record(String to, String subject) {
        log.info("[AUDIT] Notificación registrada para {} - {}", to, subject);
    }
}
