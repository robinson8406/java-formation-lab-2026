package com.indra.notifications;

import com.indra.notifications.service.EmailSender;
import com.indra.notifications.service.QueuedEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest(properties = "notification.email-sender=queued")
@ActiveProfiles("dev")
class NotificationQueuedProfileTests {

    @Test
    void propertySelectsQueuedSenderInsteadOfFakeSender(@Autowired ApplicationContext context) {
        var emailSenders = context.getBeansOfType(EmailSender.class);

        assertEquals(1, emailSenders.size());
        assertInstanceOf(QueuedEmailSender.class, emailSenders.values().iterator().next());
    }
}
