package com.indra.notifications;

import com.indra.notifications.config.NotificationProperties;
import com.indra.notifications.email.EmailSender;
import com.indra.notifications.email.FakeEmailSender;
import com.indra.notifications.email.NoOpEmailSender;
import com.indra.notifications.email.SmtpEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("dev")
class NotificationServiceDevProfileTest {

    private final EmailSender emailSender;
    private final ApplicationContext context;
    private final NotificationProperties properties;

    @Autowired
    NotificationServiceDevProfileTest(EmailSender emailSender, ApplicationContext context,
            NotificationProperties properties) {
        this.emailSender = emailSender;
        this.context = context;
        this.properties = properties;
    }

    @Test
    void devProfileActivatesFakeEmailSender() {
        assertThat(emailSender).isInstanceOf(FakeEmailSender.class);
    }

    @Test
    void devProfileRegistersOnlyFakeEmailSender() {
        assertThat(context.getBeansOfType(EmailSender.class).values())
                .singleElement()
                .isInstanceOf(FakeEmailSender.class);
        assertThat(context.getBeansOfType(SmtpEmailSender.class)).isEmpty();
        assertThat(context.getBeansOfType(NoOpEmailSender.class)).isEmpty();
    }

    @Test
    void devProfileLoadsItsOwnRetryAttempts() {
        assertThat(properties.getRetryAttempts()).isEqualTo(1);
    }

    @Test
    void fakeEmailSenderOnlyLogsWithoutNetwork() {
        assertThatCode(() -> emailSender.send("qa@indra.com", "subject", "body"))
                .doesNotThrowAnyException();
    }
}
