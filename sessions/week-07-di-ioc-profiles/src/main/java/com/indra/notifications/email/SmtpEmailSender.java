package com.indra.notifications.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("prod")
@Primary
@Qualifier("primaryEmailSender")
@Component
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        // Simulación: no se abre socket SMTP real.
        log.info("[SMTP] Conectando a servidor real y enviando a {}", to);
    }
}