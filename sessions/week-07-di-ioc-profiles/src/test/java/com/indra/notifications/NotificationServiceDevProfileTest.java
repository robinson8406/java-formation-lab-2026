package com.indra.notifications;

import com.indra.notifications.email.EmailSender;
import com.indra.notifications.email.FakeEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class NotificationServiceDevProfileTest {

    private final EmailSender emailSender;

    @Autowired
    NotificationServiceDevProfileTest(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Test
    void devProfileActivatesFakeEmailSender() {
        assertThat(emailSender).isInstanceOf(FakeEmailSender.class);
    }
}
