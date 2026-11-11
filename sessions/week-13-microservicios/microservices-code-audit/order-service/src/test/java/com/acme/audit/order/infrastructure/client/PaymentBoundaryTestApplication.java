package com.acme.audit.order.infrastructure.client;


import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.acme.audit.order.config.HttpClientConfiguration;
import com.acme.audit.order.infrastructure.client.PaymentHttpClient;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
    HttpClientConfiguration.class,
    PaymentHttpClient.class
})
public class PaymentBoundaryTestApplication {
}
