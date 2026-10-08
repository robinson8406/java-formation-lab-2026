package com.indra.notifications.service;

import com.indra.notifications.config.TestMailConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(TestMailConfiguration.class)
class TestConfigurationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailSender emailSender;

    @Test
    @DisplayName("Debe usar el bean provisto por @TestConfiguration sin alterar la lógica de perfiles de producción")
    void testConfiguration_registersCustomTestBean() {
        assertNotNull(notificationService);
        assertNotNull(emailSender);
        assertInstanceOf(TestMailConfiguration.TestEmailSender.class, emailSender);

        TestMailConfiguration.TestEmailSender testSender = (TestMailConfiguration.TestEmailSender) emailSender;
        notificationService.notify("cliente@test.com", "Test Asunto", "Mensaje Test");

        assertEquals(1, testSender.getSentMessages().size());
        assertEquals("cliente@test.com|Test Asunto|Mensaje Test", testSender.getSentMessages().get(0));
    }
}
