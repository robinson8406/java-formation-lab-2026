package com.indra.catalog.suppliers.application;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import com.indra.catalog.suppliers.domain.SupplierRepository;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;
    private final AtomicLong sequence = new AtomicLong();

    public SupplierServiceImpl(SupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    public Supplier create(Supplier supplier) {
        Supplier created = new Supplier(
                "SUP-%03d".formatted(sequence.incrementAndGet()),
                supplier.name(),
                supplier.taxId(),
                supplier.email(),
                "Creado vía API el " + Instant.now());
        return repository.save(created);
    }

    @Override
    public Supplier findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }

    @Override
    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new SupplierNotFoundException(id);
        }
    }
}
