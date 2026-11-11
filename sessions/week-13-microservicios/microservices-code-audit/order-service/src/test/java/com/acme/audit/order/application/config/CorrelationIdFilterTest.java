package com.acme.audit.order.application.config;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.acme.audit.order.config.CorrelationIdFilter;

import jakarta.servlet.FilterChain;

class CorrelationIdFilterTest {

    private static final String VALID_CORRELATION_ID = "ORDER-123:PAYMENT_456";

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        ThreadContext.clearAll();
    }

    @AfterEach
    void tearDown() {
        ThreadContext.clearAll();
    }

    @Test
    @DisplayName("Should propagate a valid correlation ID")
    void shouldPropagateValidCorrelationId() throws Exception {

        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                VALID_CORRELATION_ID
        );

        FilterChain chain = (servletRequest, servletResponse) -> {
            assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                    .isEqualTo(VALID_CORRELATION_ID);
        };

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .isEqualTo(VALID_CORRELATION_ID);

        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @Test
    @DisplayName("Should generate a correlation ID when request header is missing")
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {

        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            String correlationId =
                    ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY);

            // Assert inside the filter chain
            assertThat(correlationId).isNotBlank();
            assertThat(UUID.fromString(correlationId)).isNotNull();

            assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                    .isEqualTo(correlationId);
        });

        // Assert
        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid correlation id",
            "invalid@id",
            "invalid/id",
            "invalid#id",
            "invalid?id",
            ""
    })
    @DisplayName("Should generate a new correlation ID when supplied ID is invalid")
    void shouldGenerateCorrelationIdWhenSuppliedIdIsInvalid(
            String invalidCorrelationId) throws Exception {

        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                invalidCorrelationId
        );

        // Act
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {

            String correlationId =
                    ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY);

            // Assert inside the filter chain
            assertThat(correlationId)
                    .isNotEqualTo(invalidCorrelationId);

            assertThat(UUID.fromString(correlationId))
                    .isNotNull();

            assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                    .isEqualTo(correlationId);
        });

        // Assert
        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @Test
    @DisplayName("Should accept a correlation ID with maximum allowed length")
    void shouldAcceptCorrelationIdWithMaximumAllowedLength() throws Exception {

        // Arrange
        String correlationId = "A".repeat(128);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(CorrelationIdFilter.HEADER_NAME, correlationId);

        // Act
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {

            assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                    .isEqualTo(correlationId);
        });

        // Assert
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .isEqualTo(correlationId);

        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @Test
    @DisplayName("Should generate a new correlation ID when supplied ID exceeds maximum length")
    void shouldGenerateCorrelationIdWhenSuppliedIdExceedsMaximumLength()
            throws Exception {

        // Arrange
        String correlationId = "A".repeat(129);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(CorrelationIdFilter.HEADER_NAME, correlationId);

        // Act
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {

            String generatedId =
                    ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY);

            assertThat(UUID.fromString(generatedId))
                    .isNotNull();

            assertThat(generatedId).isNotEqualTo(correlationId);
        });

        // Assert
        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @Test
    @DisplayName("Should clear correlation ID from ThreadContext when filter chain fails")
    void shouldClearCorrelationIdWhenFilterChainFails() {

        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                VALID_CORRELATION_ID
        );

        IOException expectedException =
                new IOException("Downstream processing failed");

        FilterChain failingChain = (servletRequest, servletResponse) -> {
            assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                    .isEqualTo(VALID_CORRELATION_ID);

            throw expectedException;
        };

        // Act & Assert
        assertThatThrownBy(() -> filter.doFilter(
                request,
                response,
                failingChain
        ))
                .isSameAs(expectedException);

        // Verify cleanup despite the exception
        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }
}
