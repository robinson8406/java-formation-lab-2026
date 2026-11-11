package com.acme.audit.payment.config;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;

@Component
public class ProviderRetryEventLogger {

    private static final Logger log =
            LogManager.getLogger(ProviderRetryEventLogger.class);

    private static final String RETRY_INSTANCE = "providerService";

    private final RetryRegistry retryRegistry;

    public ProviderRetryEventLogger(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }

    @PostConstruct
    public void registerRetryEventListeners() {

        Retry retry = retryRegistry.retry(RETRY_INSTANCE);

        retry.getEventPublisher()

            .onRetry(event -> log.warn(
                "PROVIDER_RETRY | event=RETRY_TRIGGERED | instance={} | retryNumber={} | totalAttempts={} | cause={}",
                event.getName(),
                event.getNumberOfRetryAttempts(),
                event.getNumberOfRetryAttempts() + 1,
                getExceptionName(event.getLastThrowable())
            ))

            .onSuccess(event -> log.info(
                "PROVIDER_RETRY | event=RETRY_SUCCESS | instance={} | retriesPerformed={} | totalAttempts={}",
                event.getName(),
                event.getNumberOfRetryAttempts(),
                event.getNumberOfRetryAttempts() + 1
            ))

            .onError(event -> log.error(
                "PROVIDER_RETRY | event=RETRY_EXHAUSTED | instance={} | retriesPerformed={} | totalAttempts={} | cause={}",
                event.getName(),
                event.getNumberOfRetryAttempts(),
                event.getNumberOfRetryAttempts() + 1,
                getExceptionName(event.getLastThrowable())
            ))

            .onIgnoredError(event -> log.warn(
            		"PROVIDER_RETRY | event=ERROR_IGNORED | instance={} | cause={}",
            		event.getName(),
                getExceptionName(event.getLastThrowable())
            ));

        log.info(
            "PROVIDER_RETRY | event=LISTENER_REGISTERED | instance={}",
           RETRY_INSTANCE
        );
    }

    private String getExceptionName(Throwable throwable) {
        return throwable != null
                ? throwable.getClass().getSimpleName()
                : "UNKNOWN";
    }
}
