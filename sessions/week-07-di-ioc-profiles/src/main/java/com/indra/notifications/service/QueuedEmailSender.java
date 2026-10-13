package com.indra.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(
        name = "notification.email-sender",
        havingValue = "queued"
)
public class QueuedEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuedEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        LOGGER.info("[QUEUE SIMULADA] Correo agregado a la cola local; no se establece conexión externa");
    }
}
