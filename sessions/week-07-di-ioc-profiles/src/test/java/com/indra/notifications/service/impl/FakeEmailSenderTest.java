package com.indra.notifications.service.impl;

import com.indra.notifications.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "local.email.server.enabled=false"
})
@ActiveProfiles("dev")
class FakeEmailSenderTest {
    

    private final EmailSender emailSender;

    @Autowired
    public FakeEmailSenderTest(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Test
    void devProfileUsesFakeEmailSender() {
        assertInstanceOf(FakeEmailSender.class, emailSender);
    }
}