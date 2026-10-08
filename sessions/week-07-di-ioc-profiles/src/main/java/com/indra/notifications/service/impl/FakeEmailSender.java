package com.indra.notifications.service.impl;

import com.indra.notifications.service.EmailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    name = "local.email.server.enabled",
    havingValue = "false"
)
@Profile("dev")
public class FakeEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("[FAKE EMAIL] Enviando correo a " + to +" con asunto: " + subject
        );
    }
}