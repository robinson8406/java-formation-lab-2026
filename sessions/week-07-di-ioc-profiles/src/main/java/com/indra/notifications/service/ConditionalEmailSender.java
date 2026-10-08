package com.indra.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("qa")
@ConditionalOnProperty(prefix = "notification.email", name = "provider", havingValue = "conditional")
public class ConditionalEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ConditionalEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[CONDITIONAL] Enviando email condicional a {}: {}", to, subject);
    }
}
