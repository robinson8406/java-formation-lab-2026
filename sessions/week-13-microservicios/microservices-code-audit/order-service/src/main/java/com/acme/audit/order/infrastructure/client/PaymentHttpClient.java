package com.acme.audit.order.infrastructure.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.acme.audit.order.application.port.out.PaymentGateway;

@Component
public class PaymentHttpClient implements PaymentGateway {

    private static final Logger log =
            LogManager.getLogger(PaymentHttpClient.class);

    private static final String PAYMENTS_PATH = "/payments";

    private final RestClient restClient;

    public PaymentHttpClient(
    		 @Qualifier("paymentRestClient") RestClient restClient) {

        this.restClient = restClient;
    }

    @Override
    public PaymentOutcome charge(
            String key,
            String orderId,
            BigDecimal amount,
            String currency) {

        var request = new PaymentRequest(orderId, amount, currency);

        log.info(
                "PAYMENT_HTTP | event=REQUEST_SENT | orderId={} | idempotencyKey={}",
                orderId,
                key
        );

        try {
            PaymentResponse body = restClient.post()
                    .uri(PAYMENTS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", key)
                    .header("X-Correlation-Id", correlationId())
                    .body(request)
                    .retrieve()
                    .body(PaymentResponse.class);

            if (body == null) {
                throw new IllegalStateException(
                        "Payment Service returned an empty response"
                );
            }

            log.info(
                    "PAYMENT_HTTP | event=RESPONSE_RECEIVED | orderId={} | idempotencyKey={} | paymentId={} | status={} | replayed={}",
                    orderId,
                    key,
                    body.paymentId(),
                    body.status(),
                    body.replayed()
            );

            return new PaymentOutcome(
                    body.paymentId(),
                    body.status(),
                    body.replayed()
            );

        } catch (RestClientException exception) {

        	IOException communicationCause =
                    findCause(exception, IOException.class);

            // No todos los RestClientException son fallos de comunicación.
            // Por ejemplo, un error de conversión o un HTTP 4xx no debe
            // convertirse automáticamente en un reintento.
            if (communicationCause == null) {
                throw exception;
            }
            log.error(
                    "PAYMENT_HTTP | event=COMMUNICATION_FAILURE | orderId={} | idempotencyKey={} | cause={}",
                    orderId,
                    key,
                    communicationCause.getClass().getSimpleName()
                    
            );

            throw new PaymentCommunicationException(
                    "Communication failure calling payment provider",
                    exception
            );
        }
    }

    private String correlationId() {
        String id = ThreadContext.get("idTx");

        return id == null || id.isBlank()
                ? UUID.randomUUID().toString()
                : id;
    }

    private record PaymentRequest(
            String orderId,
            BigDecimal amount,
            String currency) {
    }

    private record PaymentResponse(
            String paymentId,
            String orderId,
            String status,
            boolean replayed) {
    }
    
    private <T extends Throwable> T findCause(
            Throwable error,
            Class<T> expectedType) {

        Throwable current = error;

        while (current != null) {

            if (expectedType.isInstance(current)) {
                return expectedType.cast(current);
            }

            if (current.getCause() == current) {
                break;
            }

            current = current.getCause();
        }

        return null;
    }

}
