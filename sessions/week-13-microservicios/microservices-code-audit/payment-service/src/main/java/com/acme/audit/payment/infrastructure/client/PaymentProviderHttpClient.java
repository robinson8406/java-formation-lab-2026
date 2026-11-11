
package com.acme.audit.payment.infrastructure.client;

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

import com.acme.audit.payment.application.port.out.PaymentProviderPort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class PaymentProviderHttpClient implements PaymentProviderPort {

    private static final Logger log =
            LogManager.getLogger(PaymentProviderHttpClient.class);

    private static final String CORRELATION_CONTEXT_KEY = "idTx";
    private static final String CHARGES_PATH = "/provider/charges";

    private final RestClient restClient;

    public PaymentProviderHttpClient(
            @Qualifier("providerRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @CircuitBreaker(name = "providerService")
    @Retry(name = "providerService")
    public ProviderCharge charge(
            String key,
            String orderId,
            BigDecimal amount,
            String currency) {

        String correlationId = correlationId();

        var request = new ProviderRequest(orderId, amount, currency);

        log.info(
                "PROVIDER_HTTP | idTx=[{}] | event=REQUEST_SENT | orderId={} | idempotencyKey={} | endpoint={}",
                correlationId, orderId, key, CHARGES_PATH
        );

        try {
            ProviderResponse body = restClient.post()
                    .uri(CHARGES_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", key)
                    .header("X-Correlation-Id", correlationId)
                    .body(request)
                    .retrieve()
                    .body(ProviderResponse.class);

            if (body == null) {
                throw new IllegalStateException(
                        "Payment provider returned an empty response"
                );
            }

            log.info(
                    "PROVIDER_HTTP | idTx=[{}] | event=RESPONSE_RECEIVED | orderId={} | providerTransactionId={} | replayed={}",
                    correlationId,
                    orderId,
                    body.providerTransactionId(),
                    body.replayed()
            );

            return new ProviderCharge(
                    body.providerTransactionId(),
                    body.status()
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

            log.warn(
                    "PROVIDER_HTTP | idTx=[{}] | event=COMMUNICATION_FAILURE | orderId={} | idempotencyKey={} | cause={}",
                    correlationId,
                    orderId,
                    key,
                    communicationCause.getClass().getSimpleName()
            );

            throw new ProviderCommunicationException(
                    "Communication failure calling payment provider",
                    exception
            );
        }
    }

    private String correlationId() {
        String id = ThreadContext.get(CORRELATION_CONTEXT_KEY);

        return id == null || id.isBlank()
                ? UUID.randomUUID().toString()
                : id;
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

    private record ProviderRequest(
            String orderId,
            BigDecimal amount,
            String currency) {
    }

    private record ProviderResponse(
            String providerTransactionId,
            String orderId,
            String status,
            boolean replayed) {
    }
}
