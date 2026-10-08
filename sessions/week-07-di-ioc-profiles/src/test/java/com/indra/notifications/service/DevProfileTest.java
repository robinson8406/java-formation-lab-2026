package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
class DevProfileTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private NotificationAuditLog auditLog;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${notification.retry-attempts}")
    private int retryAttempts;

    @Test
    @DisplayName("Debe activar FakeEmailSender en perfil dev y excluir SmtpEmailSender")
    void devProfile_activatesFakeEmailSenderAndExcludesSmtp() {
        assertNotNull(notificationService);
        assertNotNull(emailSender);
        assertInstanceOf(FakeEmailSender.class, emailSender);
        assertInstanceOf(FakeEmailSender.class, notificationService.getEmailSender());

        assertTrue(applicationContext.containsBean("fakeEmailSender"));
        assertFalse(applicationContext.containsBean("smtpEmailSender"));
        assertEquals(1, retryAttempts);
    }

    @Test
    @DisplayName("Debe ejecutar notify sin errores usando el FakeEmailSender")
    void notify_executesSuccessfullyWithFakeSender() {
        notificationService.notify("dev-user@indra.es", "Alerta Dev", "Cuerpo de prueba");
        assertNotNull(auditLog);
    }
}
