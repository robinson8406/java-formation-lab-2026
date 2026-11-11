package com.acme.audit.order.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.acme.audit.order.application.port.out.PaymentGateway;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

@SpringBootTest
class PaymentBoundaryIntegrationTest {
	private static final WireMockServer SERVER = startServer();

	private static WireMockServer startServer() {
		WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
		server.start();
		return server;
	}

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("clients.payment.base-url", SERVER::baseUrl);
		registry.add("clients.payment.read-timeout", () -> "100ms");
		registry.add("clients.payment.connect-timeout", () -> "1s");
	}

	@Autowired
	PaymentGateway paymentPort;

	@AfterAll
	static void stopServer() {
		SERVER.stop();
	}

	@Test
	void paymentServiceCallIsNotRetriedByOrderService() {
		SERVER.stubFor(post(urlEqualTo("/payments")).willReturn(
				aResponse().withStatus(201).withFixedDelay(350).withHeader("Content-Type", "application/json").withBody(
						"{\"paymentId\":\"PAY-1\",\"orderId\":\"ORD-1\",\"status\":\"APPROVED\",\"replayed\":false}")));
		assertThatThrownBy(() -> paymentPort.charge("ORDER-1-PAYMENT", "ORD-1", new BigDecimal("250.00"), "COP"))
				.isInstanceOf(PaymentCommunicationException.class);
		SERVER.verify(1,
				postRequestedFor(urlEqualTo("/payments")).withHeader("Idempotency-Key", equalTo("ORDER-1-PAYMENT")));
	}
}
