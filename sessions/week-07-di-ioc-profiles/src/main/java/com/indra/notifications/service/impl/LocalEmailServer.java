package com.indra.notifications.service.impl;

import com.indra.notifications.service.EmailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@ConditionalOnProperty(
    name = "local.email.server.enabled",
    havingValue = "true"
)
@Profile("dev")
public class LocalEmailServer implements EmailSender{

    @Override
    public void send(String to, String subject, String body) {
        // Implement the email sending logic here
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }

}
