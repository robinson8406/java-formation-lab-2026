package com.indra.catalog.suppliers.application;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierIdGenerator;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import com.indra.catalog.suppliers.domain.SupplierRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class DefaultSupplierService implements SupplierService {

    private final SupplierRepository repository;
    private final SupplierIdGenerator idGenerator;
    private final Clock clock;

    public DefaultSupplierService(SupplierRepository repository, SupplierIdGenerator idGenerator, Clock clock) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public Supplier create(CreateSupplierCommand command) {
        Supplier supplier = new Supplier(idGenerator.nextId(), command.name(), command.taxId(), command.email(),
                "Creado vía API el " + Instant.now(clock));
        return repository.save(supplier);
    }

    @Override
    public Supplier findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }

    @Override
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new SupplierNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
