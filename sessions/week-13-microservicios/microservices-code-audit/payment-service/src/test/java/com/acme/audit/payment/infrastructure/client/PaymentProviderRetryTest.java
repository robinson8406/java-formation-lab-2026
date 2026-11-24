package com.acme.audit.payment.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.acme.audit.payment.application.port.out.PaymentProviderPort;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentProviderRetryTest {

    private static final String CHARGES_PATH = "/provider/charges";
    private static final String IDEMPOTENCY_KEY = "ORDER-RETRY-1-PAYMENT";
    private static final WireMockServer SERVER = startServer();

    @Autowired
    private PaymentProviderPort paymentProvider;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("clients.provider.base-url", SERVER::baseUrl);
        registry.add("clients.provider.connect-timeout", () -> "1s");
        registry.add("clients.provider.read-timeout", () -> "100ms");
    }

    @BeforeEach
    void setUp() {
        SERVER.resetAll();
        SERVER.stubFor(post(urlPathEqualTo(CHARGES_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(350)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "providerTransactionId": "PROV-1",
                                    "orderId": "ORDER-RETRY-1",
                                    "status": "APPROVED",
                                    "replayed": false
                                }
                                """)));
    }

    @AfterAll
    static void stopServer() {
        SERVER.stop();
    }

    @Test
    void retriesProviderWithSameIdempotencyKeyAndStopsAfterThreeAttempts() {
        assertThatThrownBy(() -> paymentProvider.charge(
                IDEMPOTENCY_KEY,
                "ORDER-RETRY-1",
                new BigDecimal("250.00"),
                "COP"))
                .isInstanceOf(ProviderCommunicationException.class);

        SERVER.verify(3, postRequestedFor(urlPathEqualTo(CHARGES_PATH))
                .withHeader("Idempotency-Key", equalTo(IDEMPOTENCY_KEY)));
    }

    private static WireMockServer startServer() {
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.options().dynamicPort());
        server.start();
        return server;
    }
}
