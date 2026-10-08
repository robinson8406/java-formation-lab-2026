package com.indra.notifications.email;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Dev-only sender: never opens a real network connection, only logs.
 */
@Profile("dev")
@Component
public class FakeEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("[FAKE] Simulando envío a " + to + ": " + subject + " -> " + body);
    }
}
