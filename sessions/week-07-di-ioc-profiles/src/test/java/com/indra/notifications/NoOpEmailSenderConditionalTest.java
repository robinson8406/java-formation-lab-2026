package com.indra.notifications;

import com.indra.notifications.email.EmailSender;
import com.indra.notifications.email.NoOpEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bonus @ConditionalOnProperty: con notification.email.noop-enabled=true el
 * NoOpEmailSender se registra y, por ser @Primary, reemplaza al sender del perfil.
 */
@SpringBootTest(properties = "notification.email.noop-enabled=true")
@ActiveProfiles("dev")
class NoOpEmailSenderConditionalTest {

    private final EmailSender emailSender;

    @Autowired
    NoOpEmailSenderConditionalTest(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Test
    void noOpSenderTakesPrecedenceWhenPropertyIsEnabled() {
        assertThat(emailSender).isInstanceOf(NoOpEmailSender.class);
    }
}
