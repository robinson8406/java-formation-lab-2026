package com.indra.notifications.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(name = "email.provider", havingValue = "aws")
public class AwsEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("[AWS SES] Enviando correo usando Amazon a: " + to);
    }
}

