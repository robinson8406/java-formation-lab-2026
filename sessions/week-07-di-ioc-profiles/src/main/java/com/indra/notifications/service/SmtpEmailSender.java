package com.indra.notifications.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class SmtpEmailSender implements EmailSender {

    @Value("${email.server}")
    private String emailServer;

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("[SMTP] Conectando a " + emailServer + " y enviando a " + to);
    }
}

