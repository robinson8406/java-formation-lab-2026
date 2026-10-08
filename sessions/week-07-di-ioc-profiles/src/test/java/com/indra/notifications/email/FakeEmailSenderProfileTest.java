package com.indra.notifications.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class FakeEmailSenderProfileTest {

    private final EmailSender emailSender;

    FakeEmailSenderProfileTest(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Test
    void shouldWireFakeEmailSenderWhenDevProfileIsActive() {
        assertThat(emailSender).isInstanceOf(FakeEmailSender.class);
    }
}