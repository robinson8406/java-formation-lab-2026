package com.indra.notifications.config;

import com.indra.notifications.service.EmailSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class NotificationTestConfig {

    @Bean
    @Primary
    public EmailSender spyEmailSender() {
        return new EmailSender() {
            @Override
            public void send(String to, String subject, String body) {
                System.out.println("[TEST CONFIG] Simulando envío especial para pruebas unitarias hacia: " + to);
            }
        };
    }
}

