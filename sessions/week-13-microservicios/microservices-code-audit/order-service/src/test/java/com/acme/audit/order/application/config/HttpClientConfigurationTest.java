package com.acme.audit.order.application.config;



import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.acme.audit.order.config.HttpClientConfiguration;

class HttpClientConfigurationTest {

    private static final String PAYMENT_BASE_URL =
            "http://localhost:8082";

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofMillis(200);

    private static final Duration READ_TIMEOUT =
            Duration.ofMillis(700);

    private HttpClientConfiguration configuration;

    private RestClient.Builder builder;
    private RestClient.Builder clonedBuilder;
    private RestClient restClient;

    @BeforeEach
    void setUp() {

        configuration = new HttpClientConfiguration();

        builder = mock(RestClient.Builder.class);
        clonedBuilder = mock(RestClient.Builder.class);
        restClient = mock(RestClient.class);

        when(builder.clone()).thenReturn(clonedBuilder);

        when(clonedBuilder.baseUrl(PAYMENT_BASE_URL))
                .thenReturn(clonedBuilder);

        when(clonedBuilder.requestFactory(
                org.mockito.ArgumentMatchers.any(ClientHttpRequestFactory.class)
        )).thenReturn(clonedBuilder);

        when(clonedBuilder.build()).thenReturn(restClient);
    }

    @Test
    @DisplayName("Should create payment RestClient with configured base URL and request factory")
    void shouldCreatePaymentRestClientWithConfiguredSettings() {

        // Act
        RestClient result = configuration.paymentRestClient(
                builder,
                PAYMENT_BASE_URL,
                CONNECT_TIMEOUT,
                READ_TIMEOUT
        );

        // Assert
        assertThat(result).isSameAs(restClient);

        verify(builder).clone();

        verify(clonedBuilder).baseUrl(PAYMENT_BASE_URL);

        verify(clonedBuilder).requestFactory(
                org.mockito.ArgumentMatchers.argThat(
                        requestFactory ->
                                requestFactory instanceof SimpleClientHttpRequestFactory
                )
        );

        verify(clonedBuilder).build();

        verifyNoMoreInteractions(builder, clonedBuilder);
    }

    @Test
    @DisplayName("Should configure a request factory for HTTP communication")
    void shouldConfigureRequestFactoryForHttpCommunication() {

        // Act
        configuration.paymentRestClient(
                builder,
                PAYMENT_BASE_URL,
                CONNECT_TIMEOUT,
                READ_TIMEOUT
        );

        // Assert
        org.mockito.ArgumentCaptor<ClientHttpRequestFactory> factoryCaptor =
                org.mockito.ArgumentCaptor.forClass(
                        ClientHttpRequestFactory.class
                );

        verify(clonedBuilder).requestFactory(factoryCaptor.capture());

        assertThat(factoryCaptor.getValue())
                .isInstanceOf(SimpleClientHttpRequestFactory.class);
    }

    @Test
    @DisplayName("Should not modify the original shared RestClient builder")
    void shouldNotModifyOriginalRestClientBuilder() {

        // Act
        configuration.paymentRestClient(
                builder,
                PAYMENT_BASE_URL,
                CONNECT_TIMEOUT,
                READ_TIMEOUT
        );

        // Assert
        verify(builder).clone();

        verifyNoMoreInteractions(builder);
    }
}
