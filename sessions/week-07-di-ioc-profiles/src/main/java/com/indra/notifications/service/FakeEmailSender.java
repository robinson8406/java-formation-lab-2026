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
        havingValue = "fake",
        matchIfMissing = true
)
public class FakeEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        LOGGER.info("[FAKE] Simulación de envío de correo completada");
    }
}
