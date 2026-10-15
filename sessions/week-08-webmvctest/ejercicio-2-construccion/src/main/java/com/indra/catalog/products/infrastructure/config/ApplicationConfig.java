package com.indra.catalog.products.infrastructure.config;

import com.indra.catalog.products.application.ProductService;
import com.indra.catalog.products.application.ProductServiceImpl;
import com.indra.catalog.products.domain.port.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    
    @Bean
    ProductService productService(ProductRepository productRepository) {
        return new ProductServiceImpl(productRepository);
    }
}
