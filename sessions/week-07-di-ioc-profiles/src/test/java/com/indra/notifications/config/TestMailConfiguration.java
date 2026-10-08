package com.indra.notifications.config;

import com.indra.notifications.service.EmailSender;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestMailConfiguration {

    public static class TestEmailSender implements EmailSender {

        private final List<String> sentMessages = new ArrayList<>();

        @Override
        public void send(String to, String subject, String body) {
            sentMessages.add(to + "|" + subject + "|" + body);
        }

        public List<String> getSentMessages() {
            return Collections.unmodifiableList(sentMessages);
        }
    }

    @Bean
    @Primary
    public EmailSender testEmailSender() {
        return new TestEmailSender();
    }
}
