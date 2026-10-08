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
@ActiveProfiles("prod")
class ProdProfileTest {

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
    @DisplayName("Debe activar SmtpEmailSender en perfil prod y excluir FakeEmailSender")
    void prodProfile_activatesSmtpEmailSenderAndExcludesFake() {
        assertNotNull(notificationService);
        assertNotNull(emailSender);
        assertInstanceOf(SmtpEmailSender.class, emailSender);
        assertInstanceOf(SmtpEmailSender.class, notificationService.getEmailSender());

        assertTrue(applicationContext.containsBean("smtpEmailSender"));
        assertFalse(applicationContext.containsBean("fakeEmailSender"));
        assertEquals(3, retryAttempts);
    }

    @Test
    @DisplayName("Debe ejecutar notify sin errores simulando envío SMTP sin llamadas reales")
    void notify_executesSuccessfullyWithSmtpSender() {
        notificationService.notify("prod-client@indra.es", "Alerta Prod", "Cuerpo confidencial");
        assertNotNull(auditLog);
    }
}
