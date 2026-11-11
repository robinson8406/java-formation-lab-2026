package com.acme.audit.payment.config;



import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class ProviderHttpClientConfigurationTest {

    private static final String PROVIDER_BASE_URL = "http://localhost:8083";

    private static final Duration CONNECT_TIMEOUT = Duration.ofMillis(200);

    private static final Duration READ_TIMEOUT = Duration.ofMillis(700);

    private RestClient.Builder builder;
    private RestClient.Builder clonedBuilder;
    private RestClient restClient;

    private ProviderHttpClientConfiguration configuration;

    @BeforeEach
    void setUp() {
        builder = mock(RestClient.Builder.class);
        clonedBuilder = mock(RestClient.Builder.class);
        restClient = mock(RestClient.class);

        configuration = new ProviderHttpClientConfiguration();

        when(builder.clone()).thenReturn(clonedBuilder);
        when(clonedBuilder.baseUrl(PROVIDER_BASE_URL))
                .thenReturn(clonedBuilder);
        when(clonedBuilder.requestFactory(any(ClientHttpRequestFactory.class)))
                .thenReturn(clonedBuilder);
        when(clonedBuilder.build()).thenReturn(restClient);
    }

    @Test
    void shouldCreateProviderRestClientWithConfiguredBaseUrlAndRequestFactory() {
        RestClient result = configuration.providerRestClient(
                builder,
                PROVIDER_BASE_URL,
                CONNECT_TIMEOUT,
                READ_TIMEOUT
        );

        assertThat(result).isSameAs(restClient);

        ArgumentCaptor<ClientHttpRequestFactory> factoryCaptor =
                ArgumentCaptor.forClass(ClientHttpRequestFactory.class);

        InOrder inOrder = inOrder(builder, clonedBuilder);

        inOrder.verify(builder).clone();
        inOrder.verify(clonedBuilder).baseUrl(PROVIDER_BASE_URL);
        inOrder.verify(clonedBuilder).requestFactory(factoryCaptor.capture());
        inOrder.verify(clonedBuilder).build();

        assertThat(factoryCaptor.getValue())
                .isInstanceOf(SimpleClientHttpRequestFactory.class);

        verifyNoMoreInteractions(builder, clonedBuilder);
    }

    @Test
    void shouldUseClonedBuilderWithoutModifyingOriginalBuilder() {
        configuration.providerRestClient(
                builder,
                PROVIDER_BASE_URL,
                CONNECT_TIMEOUT,
                READ_TIMEOUT
        );

        org.mockito.Mockito.verify(builder).clone();

        verifyNoMoreInteractions(builder);
    }
}
