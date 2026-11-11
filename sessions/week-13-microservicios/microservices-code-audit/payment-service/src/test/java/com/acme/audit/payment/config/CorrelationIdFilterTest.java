package com.acme.audit.payment.config;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class CorrelationIdFilterTest {

    private static final String VALID_CORRELATION_ID = "payment-123:transaction_456";

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        ThreadContext.clearAll();
        filter = new CorrelationIdFilter();
    }

    @AfterEach
    void tearDown() {
        ThreadContext.clearAll();
    }

    @Test
    void shouldPreserveValidCorrelationIdFromRequest() throws Exception {
        MockHttpServletRequest request = createRequest(VALID_CORRELATION_ID);
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (servletRequest, servletResponse) -> {
            assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                    .isEqualTo(VALID_CORRELATION_ID);
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .isEqualTo(VALID_CORRELATION_ID);

        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @Test
    void shouldGenerateCorrelationIdWhenRequestHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (servletRequest, servletResponse) -> {
            String correlationId = ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY);

            assertThat(correlationId)
                    .isNotBlank();

            assertThat(UUID.fromString(correlationId))
                    .isNotNull();
        };

        filter.doFilter(request, response, chain);

        String responseCorrelationId =
                response.getHeader(CorrelationIdFilter.HEADER_NAME);

        assertThat(UUID.fromString(responseCorrelationId))
                .isNotNull();

        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "invalid@id",
            "invalid/id",
            "invalid#id",
            "invalid?id",
            "correlation id"
    })
    void shouldGenerateCorrelationIdWhenRequestHeaderIsInvalid(String suppliedId)
            throws Exception {

        MockHttpServletRequest request = createRequest(suppliedId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            String correlationId = ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY);

            assertThat(UUID.fromString(correlationId))
                    .isNotNull();
        });

        String responseCorrelationId =
                response.getHeader(CorrelationIdFilter.HEADER_NAME);

        assertThat(UUID.fromString(responseCorrelationId))
                .isNotNull();

        assertThat(responseCorrelationId)
                .isNotEqualTo(suppliedId);

        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @Test
    void shouldAcceptCorrelationIdWithMaximumAllowedLength() throws Exception {
        String correlationId = "a".repeat(128);

        MockHttpServletRequest request = createRequest(correlationId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                    .isEqualTo(correlationId);
        });

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .isEqualTo(correlationId);
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderExceedsMaximumAllowedLength()
            throws Exception {

        String suppliedId = "a".repeat(129);

        MockHttpServletRequest request = createRequest(suppliedId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            String correlationId = ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY);

            assertThat(UUID.fromString(correlationId))
                    .isNotNull();
        });

        String responseCorrelationId =
                response.getHeader(CorrelationIdFilter.HEADER_NAME);

        assertThat(UUID.fromString(responseCorrelationId))
                .isNotNull();

        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    @Test
    void shouldRemoveCorrelationIdFromThreadContextWhenFilterChainFails() {
        MockHttpServletRequest request = createRequest(VALID_CORRELATION_ID);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) -> {
                    assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                            .isEqualTo(VALID_CORRELATION_ID);

                    throw new IOException("Downstream processing failed");
                }))
                .isInstanceOf(IOException.class)
                .hasMessage("Downstream processing failed");

        assertThat(ThreadContext.get(CorrelationIdFilter.CONTEXT_KEY))
                .isNull();
    }

    private MockHttpServletRequest createRequest(String correlationId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, correlationId);
        return request;
    }
}
