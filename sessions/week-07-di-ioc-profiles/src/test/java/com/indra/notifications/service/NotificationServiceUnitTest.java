package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private NotificationAuditLog auditLog;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(emailSender, auditLog);
    }

    @Test
    @DisplayName("Debe delegar el envío al EmailSender inyectado y registrar la auditoría sin requerir Spring")
    void notify_delegatesToEmailSenderAndRecordsAudit() {
        String to = "usuario@indra.es";
        String subject = "Factura pendiente";
        String body = "Estimado cliente, su factura ha sido emitida.";

        notificationService.notify(to, subject, body);

        verify(emailSender).send(to, subject, body);
        verify(auditLog).record(to, subject);
        assertEquals(emailSender, notificationService.getEmailSender());
    }
}
