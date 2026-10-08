package com.indra.notifications;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.indra.notifications.sender.EmailSender;

@SpringBootTest
@ActiveProfiles("dev")
class NotificationApplicationTests {

    @Autowired
    private EmailSender emailSender;

    @Test
    @DisplayName("test active profiles")
    void TestEmailSender() {
        String to = "to@c.co";
        String subject = "subject mail";
        String body = " body info";

        assertDoesNotThrow(() -> emailSender.send(to, subject, body));
    }

}
