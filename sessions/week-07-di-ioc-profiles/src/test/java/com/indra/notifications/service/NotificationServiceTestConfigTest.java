package com.indra.notifications.service;

import com.indra.notifications.config.NotificationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(NotificationTestConfig.class)
class NotificationServiceTestConfigTest {

    @Autowired
    private EmailSender emailSender;

    @Test
    void whenTestConfigIsImported_thenSpyEmailSenderIsInjected() {
        // Verificar que el bean inyectado no es ni el de dev ni el de prod, 
        // sino el anónimo creado en nuestra @TestConfiguration
        assertThat(emailSender.getClass().getName()).contains("NotificationTestConfig");
    }
}

