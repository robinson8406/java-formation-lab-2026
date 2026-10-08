package com.indra.notifications.service.impl;

import com.indra.notifications.service.EmailSender;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class SmtpEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("[SMTP EMAIL] Enviando correo a " + to + " con asunto: " + subject);
    }
}
