package com.indra.notifications.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("prod")
class PrimaryChannelResolutionTest {

    private final EmailSender emailSender;

    PrimaryChannelResolutionTest(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Test
    void shouldResolvePrimaryChannelEvenWhenSecondaryChannelIsEnabled() {
        assertThat(emailSender).isInstanceOf(SmtpEmailSender.class);
    }
}