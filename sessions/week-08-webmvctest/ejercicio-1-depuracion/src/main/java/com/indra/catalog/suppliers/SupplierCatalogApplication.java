package com.indra.catalog.suppliers;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SupplierCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupplierCatalogApplication.class, args);
    }

    @Bean
    Clock clock() {
         return Clock.systemDefaultZone();
    }
}
