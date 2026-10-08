package com.indra.notifications.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class NotificationServiceProfileTest {

    @Autowired
    private EmailSender emailSender;

    @Test
    void whenDevProfileIsActive_thenFakeEmailSenderIsInjected() {
        assertThat(emailSender).isInstanceOf(FakeEmailSender.class);
    }
}

