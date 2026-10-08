package com.indra.notifications.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("secondaryEmailSender")
@ConditionalOnProperty(prefix = "notification.channel.secondary", name = "enabled", havingValue = "true")
public class LoggingSecondaryEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSecondaryEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[SECONDARY-CHANNEL] to={}, subject='{}'", to, subject);
    }
}
