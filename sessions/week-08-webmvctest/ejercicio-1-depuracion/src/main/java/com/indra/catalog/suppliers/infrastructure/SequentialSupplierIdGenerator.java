package com.indra.catalog.suppliers.infrastructure;

import com.indra.catalog.suppliers.domain.SupplierIdGenerator;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SequentialSupplierIdGenerator implements SupplierIdGenerator {

    private final AtomicLong sequence = new AtomicLong();

    @Override
    public String nextId() {
        return "SUP-%03d".formatted(sequence.incrementAndGet());
    }
}
