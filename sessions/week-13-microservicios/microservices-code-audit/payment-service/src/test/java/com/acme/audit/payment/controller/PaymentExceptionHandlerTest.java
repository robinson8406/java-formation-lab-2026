package com.acme.audit.payment.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.acme.audit.payment.infrastructure.client.ProviderCommunicationException;

class PaymentExceptionHandlerTest {

    private final PaymentExceptionHandler handler = new PaymentExceptionHandler();

    @Test
    void shouldExposeUnknownProviderResultAsRetryableServiceUnavailable() {
        ResponseEntity<java.util.Map<String, Object>> response =
                handler.providerResultUnknown(
                        new ProviderCommunicationException(
                                "Provider timeout",
                                new RuntimeException("socket timeout")
                        )
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody())
                .containsEntry("code", "PAYMENT_PROVIDER_RESULT_UNKNOWN")
                .containsEntry(
                        "message",
                        "Payment provider result is unknown; retry with the same Idempotency-Key"
                );
    }
}
