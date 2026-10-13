package com.indra.notifications;

import com.indra.notifications.service.EmailSender;
import com.indra.notifications.service.FakeEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
@ActiveProfiles("dev")
class NotificationApplicationTests {

    @Test
    void devProfileUsesOnlyFakeEmailSender(ApplicationContext context) {
        var emailSenders = context.getBeansOfType(EmailSender.class);

        assertEquals(1, emailSenders.size());
        assertInstanceOf(FakeEmailSender.class, emailSenders.values().iterator().next());
    }
}
