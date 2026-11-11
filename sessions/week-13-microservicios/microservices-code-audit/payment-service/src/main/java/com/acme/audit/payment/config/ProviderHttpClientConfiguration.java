package com.acme.audit.payment.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ProviderHttpClientConfiguration {
	
	@Bean
    public RestClient providerRestClient(
            RestClient.Builder builder,
            @Value("${clients.provider.base-url}") String baseUrl,
            @Value("${clients.provider.connect-timeout:200ms}") Duration connectTimeout,
            @Value("${clients.provider.read-timeout:700ms}") Duration readTimeout) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
	
  
}
