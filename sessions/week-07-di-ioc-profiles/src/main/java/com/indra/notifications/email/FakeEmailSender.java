package com.indra.notifications.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Primary
@Qualifier("primaryEmailSender")
@Component
public class FakeEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(FakeEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[FAKE] Simulando envío a {}: {} -> {}", to, subject, body);
    }
}