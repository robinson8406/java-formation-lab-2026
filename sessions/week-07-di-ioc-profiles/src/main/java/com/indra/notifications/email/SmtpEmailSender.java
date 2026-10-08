package com.indra.notifications.email;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Prod sender: represents the real SMTP connection (simulated here, no real socket opened).
 */
@Profile("prod")
@Component
public class SmtpEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("[SMTP] Conectando a servidor real y enviando a " + to
                + " - asunto: " + subject);
    }
}
