package com.indra.notifications;

import com.indra.notifications.service.EmailSender;
import com.indra.notifications.service.SmtpEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
@ActiveProfiles("prod")
class NotificationProdProfileTests {

    @Test
    void prodProfileUsesOnlySimulatedSmtpEmailSender(@Autowired ApplicationContext context) {
        var emailSenders = context.getBeansOfType(EmailSender.class);

        assertEquals(1, emailSenders.size());
        assertInstanceOf(SmtpEmailSender.class, emailSenders.values().iterator().next());
    }
}
