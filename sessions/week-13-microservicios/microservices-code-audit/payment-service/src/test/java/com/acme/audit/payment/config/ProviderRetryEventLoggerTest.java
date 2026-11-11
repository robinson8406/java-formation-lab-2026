package com.acme.audit.payment.config;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

class ProviderRetryEventLoggerTest {

    private static final String RETRY_INSTANCE = "providerService";

    private static final String LOGGER_NAME =
            ProviderRetryEventLogger.class.getName();

    private LoggerContext loggerContext;
    private Configuration configuration;
    private TestLogAppender appender;

    @BeforeEach
    void setUp() {
        loggerContext = (LoggerContext) LogManager.getContext(false);
        configuration = loggerContext.getConfiguration();

        appender = new TestLogAppender("ProviderRetryEventLoggerTestAppender");
        appender.start();

        LoggerConfig loggerConfig = new LoggerConfig(
                LOGGER_NAME,
                Level.ALL,
                false
        );

        loggerConfig.addAppender(appender, Level.ALL, null);

        configuration.addLogger(LOGGER_NAME, loggerConfig);
        loggerContext.updateLoggers();
    }

    @AfterEach
    void tearDown() {
        configuration.removeLogger(LOGGER_NAME);
        loggerContext.updateLoggers();

        appender.stop();
    }

    @Test
    void shouldRegisterRetryEventListeners() {
        ProviderRetryEventLogger eventLogger =
                createEventLogger(
                        createRetryRegistry(),
                        CircuitBreakerRegistry.ofDefaults()
                );

        eventLogger.registerRetryEventListeners();

        assertThat(appender.messages())
                .anySatisfy(message -> {
                    assertThat(message)
                            .contains(
                                    "PROVIDER_RETRY",
                                    "event=LISTENER_REGISTERED",
                                    "instance=" + RETRY_INSTANCE
                            );
                });
    }

    @Test
    void shouldLogRetryTriggeredAndSuccessfulRetry() {
        Retry retry = createRegisteredRetry();

        Supplier<String> operation = new Supplier<>() {
            private int attempts;

            @Override
            public String get() {
                attempts++;

                if (attempts == 1) {
                    throw new IllegalStateException("Temporary provider failure");
                }

                return "PAYMENT_APPROVED";
            }
        };

        String result = retry.executeSupplier(operation);

        assertThat(result).isEqualTo("PAYMENT_APPROVED");

        assertThat(appender.messages())
                .anySatisfy(message -> assertThat(message)
                        .contains(
                                "event=RETRY_TRIGGERED",
                                "instance=" + RETRY_INSTANCE,
                                "retryNumber=1",
                                "totalAttempts=2",
                                "cause=IllegalStateException"
                        ))
                .anySatisfy(message -> assertThat(message)
                        .contains(
                                "event=RETRY_SUCCESS",
                                "instance=" + RETRY_INSTANCE,
                                "retriesPerformed=1",
                                "totalAttempts=2"
                        ));
    }

    @Test
    void shouldLogIgnoredErrorWithoutRetrying() {
        Retry retry = createRegisteredRetry();

        assertThatThrownBy(() -> retry.executeSupplier(() -> {
            throw new IllegalArgumentException("Invalid payment request");
        }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid payment request");

        assertThat(appender.messages())
                .anySatisfy(message -> assertThat(message)
                        .contains(
                                "event=ERROR_IGNORED",
                                "instance=" + RETRY_INSTANCE,
                                "cause=IllegalArgumentException"
                        ));

        assertThat(appender.messages())
                .noneMatch(message -> message.contains("event=RETRY_TRIGGERED"))
                .noneMatch(message -> message.contains("event=RETRY_EXHAUSTED"));
    }

    @Test
    void shouldLogRetryExhausted() {
        Retry retry = createRegisteredRetry();

        assertThatThrownBy(() -> retry.executeSupplier(() -> {
            throw new IllegalStateException("Provider unavailable");
        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Provider unavailable");

        assertThat(appender.messages())
                .anySatisfy(message -> assertThat(message)
                        .contains(
                                "event=RETRY_EXHAUSTED",
                                "instance=" + RETRY_INSTANCE,
                                "retriesPerformed=2",
                                "totalAttempts=3",
                                "cause=IllegalStateException"
                        ));
    }

    @Test
    void shouldLogCircuitBreakerOpenAndRejectedCall() {
        var circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker(RETRY_INSTANCE);
        ProviderRetryEventLogger eventLogger =
                createEventLogger(createRetryRegistry(), circuitBreakerRegistry);

        eventLogger.registerRetryEventListeners();
        circuitBreaker.transitionToOpenState();
        assertThatThrownBy(circuitBreaker::acquirePermission)
                .isInstanceOf(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class);

        assertThat(appender.messages())
                .anySatisfy(message -> assertThat(message)
                        .contains(
                                "event=STATE_TRANSITION",
                                "instance=" + RETRY_INSTANCE,
                                "to=OPEN"
                        ))
                .anySatisfy(message -> assertThat(message)
                        .contains(
                                "event=CALL_NOT_PERMITTED",
                                "instance=" + RETRY_INSTANCE
                        ));
    }

    private Retry createRegisteredRetry() {
        RetryRegistry registry = createRetryRegistry();

        ProviderRetryEventLogger eventLogger =
                createEventLogger(registry, CircuitBreakerRegistry.ofDefaults());
        eventLogger.registerRetryEventListeners();

        return registry.retry(RETRY_INSTANCE);
    }

    private ProviderRetryEventLogger createEventLogger(
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        return new ProviderRetryEventLogger(
                retryRegistry,
                circuitBreakerRegistry
        );
    }

    private RetryRegistry createRetryRegistry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ZERO)
                .retryOnException(exception ->
                        exception instanceof IllegalStateException)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return RetryRegistry.of(retryConfig);
    }

    private static class TestLogAppender extends AbstractAppender {

        private final List<String> messages = new CopyOnWriteArrayList<>();

        protected TestLogAppender(String name) {
            super(name, null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        List<String> messages() {
            return List.copyOf(messages);
        }
    }
}
