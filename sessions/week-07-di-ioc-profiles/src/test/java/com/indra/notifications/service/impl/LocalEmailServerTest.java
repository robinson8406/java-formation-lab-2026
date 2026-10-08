package com.indra.notifications.service.impl;

import com.indra.notifications.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest(properties = {
        "local.email.server.enabled=true"
})
@ActiveProfiles("dev")
class LocalEmailServerTest {

    private final EmailSender emailSender;

    @Autowired
    public LocalEmailServerTest(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Test
    void localEmailServerIsUsed() {
        assertInstanceOf(LocalEmailServer.class, emailSender);
    }
}
