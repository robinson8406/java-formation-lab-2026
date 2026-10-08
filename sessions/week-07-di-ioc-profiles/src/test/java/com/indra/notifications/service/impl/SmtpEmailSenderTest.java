package com.indra.notifications.service.impl;

import com.indra.notifications.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("prod")
class SmtpEmailSenderTest {


    private final EmailSender emailSender;

    @Autowired
    SmtpEmailSenderTest(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Test
    void prodProfileUsesSmtpEmailSender() {
        assertInstanceOf(SmtpEmailSender.class, emailSender);
    }
}