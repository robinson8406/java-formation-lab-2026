package com.acme.audit.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.acme.audit.order.application.port.out.OrderStore;
import com.acme.audit.order.application.port.out.OrderStore.OrderData;
import com.acme.audit.order.application.port.out.PaymentGateway;
import com.acme.audit.order.usecases.OrderApplicationServiceUseCase;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {
	@Mock
    OrderStore persistence;
	@Mock
    PaymentGateway paymentPort;
	@InjectMocks
	OrderApplicationServiceUseCase service;
    OrderData pending;

	@BeforeEach
	void setup() {
        pending = new OrderData("ORD-UNIT-1", "ORDER-ORD-UNIT-1-PAYMENT",
                new BigDecimal("250000.00"), "COP", "PAYMENT_PENDING", null);
	}

	@Test
	void successfulPaymentConfirmsOrder() {
		when(persistence.getOrCreate("ORD-UNIT-1", pending.amount(), "COP")).thenReturn(pending);
        when(paymentPort.charge(pending.idempotencyKey(), pending.orderId(), pending.amount(), "COP"))
                .thenReturn(new PaymentGateway.PaymentOutcome("PAY-1", "SUCCEEDED", false));
        OrderData confirmed = new OrderData("ORD-UNIT-1", pending.idempotencyKey(), pending.amount(), "COP", "CONFIRMED", "PAY-1");
        when(persistence.confirm("ORD-UNIT-1", "PAY-1")).thenReturn(confirmed);
        var result = service.create(new CreateOrderCommand("ORD-UNIT-1", pending.amount(), "COP"));
        assertThat(result.orderId()).isEqualTo("ORD-UNIT-1");
        verify(paymentPort).charge("ORDER-ORD-UNIT-1-PAYMENT", "ORD-UNIT-1", pending.amount(), "COP");
	}

	@Test
	void confirmedOrderDoesNotCallPaymentAgain() {
        OrderData confirmed = new OrderData("ORD-UNIT-1", pending.idempotencyKey(), pending.amount(), "COP", "CONFIRMED", "PAY-EXISTING");
        when(persistence.getOrCreate("ORD-UNIT-1", pending.amount(), "COP")).thenReturn(confirmed);
        var result = service.create(new CreateOrderCommand("ORD-UNIT-1", pending.amount(), "COP"));
        assertThat(result.paymentId()).isEqualTo("PAY-EXISTING");
		verifyNoInteractions(paymentPort);
	}
}