package com.acme.audit.order.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfiguration {

	 @Bean
	 public RestClient paymentRestClient(
	            RestClient.Builder builder,
	            @Value("${clients.payment.base-url}") String baseUrl,
	            @Value("${clients.payment.connect-timeout:200ms}") Duration connectTimeout,
	            @Value("${clients.payment.read-timeout:700ms}") Duration readTimeout) {

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