package com.indra.notifications.email;

/**
 * Abstraction over the actual email delivery mechanism, so that
 * {@code NotificationService} never needs to know which environment it runs in.
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}
