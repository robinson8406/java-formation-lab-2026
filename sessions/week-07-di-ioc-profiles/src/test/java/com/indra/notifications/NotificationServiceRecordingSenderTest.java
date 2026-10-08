package com.indra.notifications;

import com.indra.notifications.email.EmailSender;
import com.indra.notifications.email.TransientEmailException;
import com.indra.notifications.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that test-only beans can override the active profile's sender
 * without touching the dev/prod profile wiring.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Import(NotificationServiceRecordingSenderTest.RecordingSenderConfig.class)
class NotificationServiceRecordingSenderTest {

    private final EmailSender emailSender;
    private final RecordingEmailSender recordingEmailSender;
    private final NotificationService notificationService;

    @Autowired
    NotificationServiceRecordingSenderTest(EmailSender emailSender,
            RecordingEmailSender recordingEmailSender, NotificationService notificationService) {
        this.emailSender = emailSender;
        this.recordingEmailSender = recordingEmailSender;
        this.notificationService = notificationService;
    }

    @BeforeEach
    void resetRecordingSender() {
        recordingEmailSender.reset();
    }

    @Test
    void testConfigurationOverridesProfileSenderWithoutModifyingProfiles() {
        emailSender.send("qa@indra.com", "subject", "body");

        assertThat(emailSender).isSameAs(recordingEmailSender);
        assertThat(recordingEmailSender.getSentMessages()).hasSize(1);
    }

    @Test
    void notifySendsOnlyOnceWhenFirstAttemptSucceeds() {
        notificationService.notify("qa@indra.com", "subject", "body");

        assertThat(recordingEmailSender.getAttempts()).isEqualTo(1);
        assertThat(recordingEmailSender.getSentMessages()).hasSize(1);
    }

    @Test
    void notifyRetriesAfterSendFailure() {
        recordingEmailSender.setFailuresRemaining(1);

        notificationService.notify("qa@indra.com", "subject", "body");

        assertThat(recordingEmailSender.getAttempts()).isEqualTo(2);
        assertThat(recordingEmailSender.getSentMessages()).hasSize(1);
    }

    @Test
    void notifyDoesNotRetryNonTransientFailure() {
        recordingEmailSender.setNonRetryableFailure(new IllegalArgumentException("Invalid address"));

        assertThatThrownBy(() -> notificationService.notify("qa@indra.com", "subject", "body"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid address");
        assertThat(recordingEmailSender.getAttempts()).isEqualTo(1);
        assertThat(recordingEmailSender.getSentMessages()).isEmpty();
    }

    @TestConfiguration
    static class RecordingSenderConfig {

        @Bean
        @Primary
        RecordingEmailSender recordingEmailSender() {
            return new RecordingEmailSender();
        }
    }

    static class RecordingEmailSender implements EmailSender {

        private final List<String> sentMessages = new ArrayList<>();
        private int attempts;
        private int failuresRemaining;
        private RuntimeException nonRetryableFailure;

        @Override
        public void send(String to, String subject, String body) {
            attempts++;
            if (failuresRemaining > 0) {
                failuresRemaining--;
                throw new TransientEmailException("Simulated send failure");
            }
            if (nonRetryableFailure != null) {
                throw nonRetryableFailure;
            }
            sentMessages.add(to);
        }

        List<String> getSentMessages() {
            return sentMessages;
        }

        int getAttempts() {
            return attempts;
        }

        void setFailuresRemaining(int failuresRemaining) {
            this.failuresRemaining = failuresRemaining;
        }

        void setNonRetryableFailure(RuntimeException failure) {
            this.nonRetryableFailure = failure;
        }

        void reset() {
            sentMessages.clear();
            attempts = 0;
            failuresRemaining = 0;
            nonRetryableFailure = null;
        }
    }
}
