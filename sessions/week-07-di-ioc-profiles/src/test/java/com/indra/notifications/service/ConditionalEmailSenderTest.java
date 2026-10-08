package com.indra.notifications.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("qa")
class ConditionalEmailSenderTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${notification.retry-attempts}")
    private int retryAttempts;

    @Test
    @DisplayName("Debe activar ConditionalEmailSender mediante @ConditionalOnProperty en perfil qa")
    void conditionalEmailSender_activatedByProperty() {
        assertNotNull(notificationService);
        assertNotNull(emailSender);
        assertInstanceOf(ConditionalEmailSender.class, emailSender);
        assertInstanceOf(ConditionalEmailSender.class, notificationService.getEmailSender());

        assertTrue(applicationContext.containsBean("conditionalEmailSender"));
        assertEquals(2, retryAttempts);

        notificationService.notify("qa-tester@indra.es", "Test QA", "Verificación condicional");
    }
}
