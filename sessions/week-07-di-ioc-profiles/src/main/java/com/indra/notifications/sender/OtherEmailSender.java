package com.indra.notifications.sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "email", name = "service", havingValue = "other")
public class OtherEmailSender{

    private Logger log = LogManager.getLogger(OtherEmailSender.class);

}