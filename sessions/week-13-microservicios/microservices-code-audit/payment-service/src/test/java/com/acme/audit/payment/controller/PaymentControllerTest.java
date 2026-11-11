package com.acme.audit.payment.controller;



import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.acme.audit.payment.application.CreatePaymentCommand;
import com.acme.audit.payment.domain.PaymentResult;
import com.acme.audit.payment.usecases.PaymentApplicationServiceUseCase;

class PaymentControllerTest {

    private static final String IDEMPOTENCY_KEY = "ORDER-123-PAYMENT";

    private static final String ORDER_ID = "ORDER-123";

    private static final BigDecimal AMOUNT = new BigDecimal("250.00");

    private static final String CURRENCY = "COP";

    private static final String PAYMENT_ID = "PAY-123";

    private static final String PAYMENT_STATUS = "APPROVED";

    private PaymentApplicationServiceUseCase service;

    private PaymentController controller;

    @BeforeEach
    void setUp() {
        service = mock(PaymentApplicationServiceUseCase.class);
        controller = new PaymentController(service);
    }

    @Test
    void shouldCreatePaymentAndPreserveIdempotencyKey() {
        PaymentController.CreatePaymentRequest request =
                new PaymentController.CreatePaymentRequest(
                        ORDER_ID,
                        AMOUNT,
                        CURRENCY
                );

        PaymentResult paymentResult = createPaymentResult(false);

        when(service.process(
                IDEMPOTENCY_KEY,
                new CreatePaymentCommand(ORDER_ID, AMOUNT, CURRENCY)
        )).thenReturn(paymentResult);

        ResponseEntity<PaymentController.PaymentResponse> response =
                controller.create(IDEMPOTENCY_KEY, request);

        ArgumentCaptor<CreatePaymentCommand> commandCaptor =
                ArgumentCaptor.forClass(CreatePaymentCommand.class);

        verify(service).process(
                org.mockito.ArgumentMatchers.eq(IDEMPOTENCY_KEY),
                commandCaptor.capture()
        );

        assertThat(commandCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new CreatePaymentCommand(ORDER_ID, AMOUNT, CURRENCY));

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody())
                .isEqualTo(new PaymentController.PaymentResponse(
                        PAYMENT_ID,
                        ORDER_ID,
                        PAYMENT_STATUS,
                        false
                ));

        verifyNoMoreInteractions(service);
    }

    @Test
    void shouldReturnOkWhenPaymentWasAlreadyProcessed() {
        PaymentController.CreatePaymentRequest request =
                new PaymentController.CreatePaymentRequest(
                        ORDER_ID,
                        AMOUNT,
                        CURRENCY
                );

        PaymentResult paymentResult = createPaymentResult(true);

        when(service.process(
                IDEMPOTENCY_KEY,
                new CreatePaymentCommand(ORDER_ID, AMOUNT, CURRENCY)
        )).thenReturn(paymentResult);

        ResponseEntity<PaymentController.PaymentResponse> response =
                controller.create(IDEMPOTENCY_KEY, request);

        verify(service).process(
                IDEMPOTENCY_KEY,
                new CreatePaymentCommand(ORDER_ID, AMOUNT, CURRENCY)
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isEqualTo(new PaymentController.PaymentResponse(
                        PAYMENT_ID,
                        ORDER_ID,
                        PAYMENT_STATUS,
                        true
                ));

        verifyNoMoreInteractions(service);
    }

    private PaymentResult createPaymentResult(boolean replayed) {
        PaymentResult result = mock(PaymentResult.class);

        when(result.paymentId()).thenReturn(PAYMENT_ID);
        when(result.orderId()).thenReturn(ORDER_ID);
        when(result.status()).thenReturn(PAYMENT_STATUS);
        when(result.replayed()).thenReturn(replayed);

        return result;
    }
}

