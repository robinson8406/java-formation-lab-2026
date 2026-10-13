package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(NotificationServiceTests.TestSenderConfiguration.class)
class NotificationServiceTests {

    private final NotificationService notificationService;
    private final EmailSender emailSender;

    @Autowired
    NotificationServiceTests(NotificationService notificationService, EmailSender emailSender) {
        this.notificationService = notificationService;
        this.emailSender = emailSender;
    }

    @Test
    void notifyDelegatesEmailToInjectedSender() {
        notificationService.notify("user@example.com", "Subject", "Body");

        verify(emailSender).send("user@example.com", "Subject", "Body");
    }

    @TestConfiguration(proxyBeanMethods = false)
    @Import({NotificationService.class, NotificationAuditLog.class})
    static class TestSenderConfiguration {

        @Bean
        EmailSender testEmailSender() {
            return mock(EmailSender.class);
        }
    }
}
