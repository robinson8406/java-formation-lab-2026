package com.indra.catalog.suppliers.infrastructure;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** El reloj es una dependencia más: inyectarlo hace que la fecha de creación sea testeable. */
@Configuration
public class ClockConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
