package com.indra.notifications.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Sender de prod: representa la conexión SMTP real (simulada, no abre ningún socket).
 */
@Profile("prod")
@Component
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[SMTP] Envío simulado al servidor SMTP para {} - asunto: {}", to, subject);
    }
}
