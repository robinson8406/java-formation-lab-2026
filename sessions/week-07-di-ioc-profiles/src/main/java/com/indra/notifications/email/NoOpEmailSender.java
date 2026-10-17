package com.indra.notifications.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Opt-in sender for environments where notifications must be suppressed entirely
 * (e.g. demos, load tests). Disabled by default; enabling it takes precedence
 * over the profile-specific sender via {@link Primary}.
 */
@ConditionalOnProperty(prefix = "notification.email", name = "noop-enabled", havingValue = "true")
@Primary
@Component
public class NoOpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(NoOpEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[NOOP] Notificación suprimida para {}", to);
    }
}
