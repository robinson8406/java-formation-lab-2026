package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import com.indra.notifications.email.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {

    @MockBean
    @Qualifier("primaryEmailSender")
    static EmailSender mockEmailSender;

    @MockBean
    static NotificationAuditLog mockAuditLog;

    private final NotificationService notificationService;
    private final EmailSender emailSender;
    private final NotificationAuditLog auditLog;

    NotificationServiceTest(NotificationService notificationService,
                            @Qualifier("primaryEmailSender") EmailSender emailSender,
                            NotificationAuditLog auditLog) {
        this.notificationService = notificationService;
        this.emailSender = emailSender;
        this.auditLog = auditLog;
    }

    @Test
    void shouldSendNotificationAndRecordAudit() {
        notificationService.notify("user@test.com", "Alerta", "Cuerpo del mensaje");

        verify(emailSender, times(1)).send("user@test.com", "Alerta", "Cuerpo del mensaje");
        verify(auditLog, times(1)).record("user@test.com", "Alerta");
    }

    @Test
    void shouldFailAfterExhaustingRetriesWhenNoFallbackIsConfigured() {
        doThrow(new RuntimeException("fallo simulado"))
                .when(emailSender).send("fail@test.com", "Asunto", "Cuerpo");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                notificationService.notify("fail@test.com", "Asunto", "Cuerpo"));

        verify(emailSender, times(notificationService.getRetryAttempts()))
                .send("fail@test.com", "Asunto", "Cuerpo");
        verify(auditLog, never()).record("fail@test.com", "Asunto");
    }
}