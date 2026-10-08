package com.indra.notifications.sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile ("dev")
@Service
public class FakeEmailSender implements EmailSender{

    private Logger log = LogManager.getLogger(FakeEmailSender.class);

    public void send(String to, String subject, String body){
        log.info("Send email to {} with subject {}, and body {}",to, subject, body);

        log.info("Send email correct");
    }

}