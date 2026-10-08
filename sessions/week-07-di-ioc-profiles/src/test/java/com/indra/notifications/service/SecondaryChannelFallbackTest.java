package com.indra.notifications.service;

import com.indra.notifications.audit.NotificationAuditLog;
import com.indra.notifications.email.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "notification.channel.secondary.enabled=true",
        "notification.retry-attempts=1"
})
class SecondaryChannelFallbackTest {

    @MockBean
    @Qualifier("primaryEmailSender")
    static EmailSender mockPrimaryEmailSender;

    @MockBean(name = "secondaryEmailSender")
    static EmailSender mockSecondaryEmailSender;

    @MockBean
    static NotificationAuditLog mockAuditLog;

    private final NotificationService notificationService;

    SecondaryChannelFallbackTest(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Test
    void shouldNotUseFallbackWhenPrimaryChannelSucceeds() {
        notificationService.notify("user@test.com", "Alerta", "Cuerpo");

        verify(mockPrimaryEmailSender, times(1)).send("user@test.com", "Alerta", "Cuerpo");
        verify(mockSecondaryEmailSender, times(0)).send("user@test.com", "Alerta", "Cuerpo");
    }

    @Test
    void shouldUseFallbackOnlyWhenPrimaryChannelExhaustsRetries() {
        doThrow(new RuntimeException("fallo primario simulado"))
                .when(mockPrimaryEmailSender).send("fail@test.com", "Asunto", "Cuerpo");

        notificationService.notify("fail@test.com", "Asunto", "Cuerpo");

        verify(mockPrimaryEmailSender, times(1)).send("fail@test.com", "Asunto", "Cuerpo");
        verify(mockSecondaryEmailSender, times(1)).send("fail@test.com", "Asunto", "Cuerpo");
    }

    @Test
    void shouldFailWhenBothPrimaryAndFallbackChannelsExhaustRetries() {
        doThrow(new RuntimeException("fallo primario simulado"))
                .when(mockPrimaryEmailSender).send("both-fail@test.com", "Asunto", "Cuerpo");
        doThrow(new RuntimeException("fallo secundario simulado"))
                .when(mockSecondaryEmailSender).send("both-fail@test.com", "Asunto", "Cuerpo");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                notificationService.notify("both-fail@test.com", "Asunto", "Cuerpo"));

        verify(mockPrimaryEmailSender, times(1)).send("both-fail@test.com", "Asunto", "Cuerpo");
        verify(mockSecondaryEmailSender, times(1)).send("both-fail@test.com", "Asunto", "Cuerpo");
    }
}