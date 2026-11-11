package com.acme.audit.order.application.usecases;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.acme.audit.order.application.CreateOrderCommand;
import com.acme.audit.order.application.port.out.OrderStore;
import com.acme.audit.order.application.port.out.OrderStore.OrderData;
import com.acme.audit.order.application.port.out.PaymentGateway;
import com.acme.audit.order.application.port.out.PaymentGateway.PaymentOutcome;
import com.acme.audit.order.usecases.OrderApplicationServiceUseCase;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceUseCaseTest {

    private static final String ORDER_ID = "ORDER-123";

    private static final String IDEMPOTENCY_KEY = "ORDER-123-PAYMENT";

    private static final BigDecimal AMOUNT = new BigDecimal("250.00");

    private static final String CURRENCY = "COP";

    private static final String PAYMENT_ID = "PAYMENT-456";

    private static final String APPROVED = "APPROVED";

    private static final String PENDING = "PENDING";

    private static final String PAYMENT_UNAVAILABLE = "PAYMENT_UNAVAILABLE";

    @Mock
    private OrderStore orderStore;

    @Mock
    private PaymentGateway paymentGateway;

    private OrderApplicationServiceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new OrderApplicationServiceUseCase(
                orderStore,
                paymentGateway
        );
    }

    @Test
    void shouldCreateAndConfirmOrderWhenPaymentIsSuccessful() {
        CreateOrderCommand command = createCommand();

        OrderData pendingOrder = createOrder(PENDING, null);
        OrderData confirmedOrder = createOrder(APPROVED, PAYMENT_ID);

        PaymentOutcome paymentOutcome = new PaymentOutcome(
                PAYMENT_ID,
                APPROVED,
                false
        );

        when(orderStore.getOrCreate(ORDER_ID, AMOUNT, CURRENCY))
                .thenReturn(pendingOrder);

        when(paymentGateway.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        )).thenReturn(paymentOutcome);

        when(orderStore.confirm(ORDER_ID, PAYMENT_ID))
                .thenReturn(confirmedOrder);

        OrderApplicationServiceUseCase.OrderView result =
                useCase.create(command);

        assertThat(result)
                .isEqualTo(new OrderApplicationServiceUseCase.OrderView(
                        ORDER_ID,
                        APPROVED,
                        PAYMENT_ID
                ));

        InOrder inOrder = inOrder(orderStore, paymentGateway);

        inOrder.verify(orderStore)
                .getOrCreate(ORDER_ID, AMOUNT, CURRENCY);

        inOrder.verify(paymentGateway)
                .charge(
                        IDEMPOTENCY_KEY,
                        ORDER_ID,
                        AMOUNT,
                        CURRENCY
                );

        inOrder.verify(orderStore)
                .confirm(ORDER_ID, PAYMENT_ID);

        verifyNoMoreInteractions(orderStore, paymentGateway);
    }

    @Test
    void shouldReturnExistingOrderWithoutChargingPaymentAgain() {
        CreateOrderCommand command = createCommand();

        OrderData existingOrder = createOrder(APPROVED, PAYMENT_ID);

        when(orderStore.getOrCreate(ORDER_ID, AMOUNT, CURRENCY))
                .thenReturn(existingOrder);

        OrderApplicationServiceUseCase.OrderView result =
                useCase.create(command);

        assertThat(result)
                .isEqualTo(new OrderApplicationServiceUseCase.OrderView(
                        ORDER_ID,
                        APPROVED,
                        PAYMENT_ID
                ));

        verify(orderStore).getOrCreate(ORDER_ID, AMOUNT, CURRENCY);

        verify(paymentGateway, never()).charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        );

        verify(orderStore, never()).confirm(ORDER_ID, PAYMENT_ID);

        verify(orderStore, never()).markPaymentUnavailable(ORDER_ID);

        verifyNoMoreInteractions(orderStore, paymentGateway);
    }

    @Test
    void shouldPreserveIdempotencyKeyWhenChargingPayment() {
        CreateOrderCommand command = createCommand();

        OrderData pendingOrder = createOrder(PENDING, null);
        OrderData confirmedOrder = createOrder(APPROVED, PAYMENT_ID);

        PaymentOutcome paymentOutcome = new PaymentOutcome(
                PAYMENT_ID,
                APPROVED,
                false
        );

        when(orderStore.getOrCreate(ORDER_ID, AMOUNT, CURRENCY))
                .thenReturn(pendingOrder);

        when(paymentGateway.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        )).thenReturn(paymentOutcome);

        when(orderStore.confirm(ORDER_ID, PAYMENT_ID))
                .thenReturn(confirmedOrder);

        useCase.create(command);

        verify(paymentGateway).charge(
                IDEMPOTENCY_KEY,
                pendingOrder.orderId(),
                pendingOrder.amount(),
                pendingOrder.currency()
        );

        verifyNoMoreInteractions(orderStore, paymentGateway);
    }

    @Test
    void shouldMarkPaymentUnavailableWhenPaymentGatewayFails() {
        CreateOrderCommand command = createCommand();

        OrderData pendingOrder = createOrder(PENDING, null);

        ResourceAccessException communicationFailure =
                new ResourceAccessException("Payment service timeout");

        when(orderStore.getOrCreate(ORDER_ID, AMOUNT, CURRENCY))
                .thenReturn(pendingOrder);

        when(paymentGateway.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        )).thenThrow(communicationFailure);

        when(orderStore.markPaymentUnavailable(ORDER_ID))
                .thenReturn(createOrder(PAYMENT_UNAVAILABLE, null));

        assertThatThrownBy(() -> useCase.create(command))
                .isSameAs(communicationFailure);

        InOrder inOrder = inOrder(orderStore, paymentGateway);

        inOrder.verify(orderStore)
                .getOrCreate(ORDER_ID, AMOUNT, CURRENCY);

        inOrder.verify(paymentGateway)
                .charge(
                        IDEMPOTENCY_KEY,
                        ORDER_ID,
                        AMOUNT,
                        CURRENCY
                );

        inOrder.verify(orderStore)
                .markPaymentUnavailable(ORDER_ID);

        verify(orderStore, never()).confirm(ORDER_ID, PAYMENT_ID);

        verifyNoMoreInteractions(orderStore, paymentGateway);
    }

    @Test
    void shouldMarkPaymentUnavailableForAnyRestClientCommunicationException() {
        CreateOrderCommand command = createCommand();

        OrderData pendingOrder = createOrder(PENDING, null);

        RestClientException communicationFailure =
                new RestClientException("Payment communication failure");

        when(orderStore.getOrCreate(ORDER_ID, AMOUNT, CURRENCY))
                .thenReturn(pendingOrder);

        when(paymentGateway.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        )).thenThrow(communicationFailure);

        when(orderStore.markPaymentUnavailable(ORDER_ID))
                .thenReturn(createOrder(PAYMENT_UNAVAILABLE, null));

        assertThatThrownBy(() -> useCase.create(command))
                .isInstanceOf(RestClientException.class)
                .isSameAs(communicationFailure);

        verify(orderStore).markPaymentUnavailable(ORDER_ID);

        verifyNoMoreInteractions(orderStore, paymentGateway);
    }

    @Test
    void shouldPropagateCommunicationExceptionWhenPaymentConfirmationFails() {
        CreateOrderCommand command = createCommand();

        OrderData pendingOrder = createOrder(PENDING, null);

        PaymentOutcome paymentOutcome = new PaymentOutcome(
                PAYMENT_ID,
                APPROVED,
                false
        );

        ResourceAccessException confirmationFailure =
                new ResourceAccessException("Order confirmation communication failure");

        when(orderStore.getOrCreate(ORDER_ID, AMOUNT, CURRENCY))
                .thenReturn(pendingOrder);

        when(paymentGateway.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        )).thenReturn(paymentOutcome);

        when(orderStore.confirm(ORDER_ID, PAYMENT_ID))
                .thenThrow(confirmationFailure);

        when(orderStore.markPaymentUnavailable(ORDER_ID))
                .thenReturn(createOrder(PAYMENT_UNAVAILABLE, null));

        assertThatThrownBy(() -> useCase.create(command))
                .isSameAs(confirmationFailure);

        InOrder inOrder = inOrder(orderStore, paymentGateway);

        inOrder.verify(orderStore)
                .getOrCreate(ORDER_ID, AMOUNT, CURRENCY);

        inOrder.verify(paymentGateway)
                .charge(
                        IDEMPOTENCY_KEY,
                        ORDER_ID,
                        AMOUNT,
                        CURRENCY
                );

        inOrder.verify(orderStore)
                .confirm(ORDER_ID, PAYMENT_ID);

        inOrder.verify(orderStore)
                .markPaymentUnavailable(ORDER_ID);

        verifyNoMoreInteractions(orderStore, paymentGateway);
    }

    @Test
    void shouldPropagateExceptionWhenGettingOrCreatingOrderFails() {
        CreateOrderCommand command = createCommand();

        RestClientException repositoryFailure =
                new RestClientException("Order storage failure");

        when(orderStore.getOrCreate(ORDER_ID, AMOUNT, CURRENCY))
                .thenThrow(repositoryFailure);

        assertThatThrownBy(() -> useCase.create(command))
                .isSameAs(repositoryFailure);

        verify(orderStore).getOrCreate(ORDER_ID, AMOUNT, CURRENCY);

        verify(orderStore, never()).markPaymentUnavailable(ORDER_ID);

        verifyNoMoreInteractions(orderStore, paymentGateway);
    }

    @Test
    void shouldReturnConfirmedOrderDataInsteadOfPaymentProviderResponse() {
        CreateOrderCommand command = createCommand();

        OrderData pendingOrder = createOrder(PENDING, null);

        PaymentOutcome paymentOutcome = new PaymentOutcome(
                PAYMENT_ID,
                APPROVED,
                false
        );

        OrderData confirmedOrder = new OrderData(
                ORDER_ID,
                IDEMPOTENCY_KEY,
                AMOUNT,
                CURRENCY,
                "COMPLETED",
                PAYMENT_ID
        );

        when(orderStore.getOrCreate(ORDER_ID, AMOUNT, CURRENCY))
                .thenReturn(pendingOrder);

        when(paymentGateway.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        )).thenReturn(paymentOutcome);

        when(orderStore.confirm(ORDER_ID, PAYMENT_ID))
                .thenReturn(confirmedOrder);

        OrderApplicationServiceUseCase.OrderView result =
                useCase.create(command);

        assertThat(result)
                .isEqualTo(new OrderApplicationServiceUseCase.OrderView(
                        ORDER_ID,
                        "COMPLETED",
                        PAYMENT_ID
                ));

        verify(orderStore).getOrCreate(ORDER_ID, AMOUNT, CURRENCY);

        verify(paymentGateway).charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        );

        verify(orderStore).confirm(ORDER_ID, PAYMENT_ID);

        verifyNoMoreInteractions(orderStore, paymentGateway);
    }

    private CreateOrderCommand createCommand() {
        return new CreateOrderCommand(
                ORDER_ID,
                AMOUNT,
                CURRENCY
        );
    }

    private OrderData createOrder(String status, String paymentId) {
        return new OrderData(
                ORDER_ID,
                IDEMPOTENCY_KEY,
                AMOUNT,
                CURRENCY,
                status,
                paymentId
        );
    }
}
