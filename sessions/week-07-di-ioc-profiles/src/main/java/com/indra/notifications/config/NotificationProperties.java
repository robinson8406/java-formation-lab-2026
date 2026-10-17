package com.indra.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Single binding point for {@code notification.*} properties, so any new
 * environment (staging, qa, test...) only needs its own
 * {@code application-<env>.properties} file — no new Java code or wiring.
 */
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private final int retryAttempts;

    public NotificationProperties(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }
}
