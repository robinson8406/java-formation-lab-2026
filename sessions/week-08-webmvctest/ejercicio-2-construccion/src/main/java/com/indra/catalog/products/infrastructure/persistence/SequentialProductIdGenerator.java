package com.indra.catalog.products.infrastructure.persistence;

import com.indra.catalog.products.domain.port.ProductIdGenerator;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SequentialProductIdGenerator implements ProductIdGenerator {

    private final AtomicLong sequence = new AtomicLong();

    @Override
    public String nextId() {
        return "PRD-%04d".formatted(sequence.incrementAndGet());
    }
}
