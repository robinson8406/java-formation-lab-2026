package com.indra.notifications;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.indra.notifications.sender.OtherEmailSender;

@SpringBootTest
@TestPropertySource(properties = {
        "email.service=email"
})
class EmailSenderOtherTest {

    @Autowired(required = false)
    private OtherEmailSender emailSender;

    @Test
    @DisplayName("test conditional properties")
    void TestConditionalProperties() {
        assertNull(emailSender);
    }


}
