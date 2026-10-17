package com.indra.notifications;

import com.indra.notifications.config.NotificationProperties;
import com.indra.notifications.email.EmailSender;
import com.indra.notifications.email.FakeEmailSender;
import com.indra.notifications.email.SmtpEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("prod")
class NotificationServiceProdProfileTest {

    private final EmailSender emailSender;
    private final ApplicationContext context;
    private final NotificationProperties properties;

    @Autowired
    NotificationServiceProdProfileTest(EmailSender emailSender, ApplicationContext context,
            NotificationProperties properties) {
        this.emailSender = emailSender;
        this.context = context;
        this.properties = properties;
    }

    @Test
    void prodProfileActivatesSmtpEmailSender() {
        assertThat(emailSender).isInstanceOf(SmtpEmailSender.class);
    }

    @Test
    void prodProfileDoesNotRegisterFakeEmailSender() {
        assertThat(context.getBeansOfType(EmailSender.class).values())
                .singleElement()
                .isInstanceOf(SmtpEmailSender.class);
        assertThat(context.getBeansOfType(FakeEmailSender.class)).isEmpty();
    }

    @Test
    void prodProfileLoadsItsOwnRetryAttempts() {
        assertThat(properties.getRetryAttempts()).isEqualTo(5);
    }
}
