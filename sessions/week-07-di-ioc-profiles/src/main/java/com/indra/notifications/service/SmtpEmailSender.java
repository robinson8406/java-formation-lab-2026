package com.indra.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class SmtpEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        LOGGER.info("[SMTP SIMULADO] Envío de correo simulado; no se establece conexión SMTP");
    }
}
