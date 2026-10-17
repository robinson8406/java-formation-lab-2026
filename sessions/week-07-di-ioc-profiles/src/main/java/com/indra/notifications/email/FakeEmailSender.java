package com.indra.notifications.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Sender de dev: nunca abre una conexión de red, solo registra en el log.
 */
@Profile("dev")
@Component
public class FakeEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(FakeEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[FAKE] Envío simulado a {} - asunto: {} - cuerpo: {}", to, subject, body);
    }
}
