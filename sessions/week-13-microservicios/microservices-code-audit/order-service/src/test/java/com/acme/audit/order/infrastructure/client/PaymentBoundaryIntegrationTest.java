
package com.acme.audit.order.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.acme.audit.order.application.port.out.PaymentGateway;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

@SpringBootTest(
    classes = PaymentBoundaryTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class PaymentBoundaryIntegrationTest {

    private static final String PAYMENTS_PATH = "/payments";
    private static final String IDEMPOTENCY_KEY = "ORDER-1-PAYMENT";
    private static final String ORDER_ID = "ORD-1";

    private static final Duration RESPONSE_DELAY = Duration.ofMillis(350);
    private static final Duration POLL_DELAY = Duration.ofSeconds(1);
    private static final Duration VERIFICATION_TIMEOUT = Duration.ofSeconds(2);

    private static final WireMockServer SERVER = startServer();

    @Autowired
    private PaymentGateway paymentGateway;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("clients.payment.base-url", SERVER::baseUrl);
        registry.add("clients.payment.connect-timeout", () -> "1s");
        registry.add("clients.payment.read-timeout", () -> "100ms");
    }

    @BeforeEach
    void setUp() {
        SERVER.resetAll();

        SERVER.stubFor(
            post(urlPathEqualTo(PAYMENTS_PATH))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withFixedDelay((int) RESPONSE_DELAY.toMillis())
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "paymentId": "PAY-1",
                                "orderId": "ORD-1",
                                "status": "APPROVED",
                                "replayed": false
                            }
                            """)
                )
        );
    }

    @AfterAll
    static void stopServer() {
        SERVER.stop();
    }

    @Test
    void paymentRequestMustNotBeRetriedByOrderService() {

        assertThatThrownBy(() ->
            paymentGateway.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                new BigDecimal("250.00"),
                "COP"
            )
        ).isInstanceOf(PaymentCommunicationException.class);

        await()
            .pollDelay(POLL_DELAY)
            .atMost(VERIFICATION_TIMEOUT)
            .untilAsserted(() ->
                SERVER.verify(
                    1,
                    postRequestedFor(urlPathEqualTo(PAYMENTS_PATH))
                        .withHeader(
                            "Idempotency-Key",
                            equalTo(IDEMPOTENCY_KEY)
                        )
                )
            );
    }

    private static WireMockServer startServer() {
        WireMockServer server = new WireMockServer(
            WireMockConfiguration.options().dynamicPort()
        );

        server.start();
        return server;
    }
}
