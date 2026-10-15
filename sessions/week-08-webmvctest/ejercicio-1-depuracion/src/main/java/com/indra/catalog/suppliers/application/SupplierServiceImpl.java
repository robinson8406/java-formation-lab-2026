package com.indra.catalog.suppliers.application;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import com.indra.catalog.suppliers.domain.SupplierRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl implements SupplierService{

    private final SupplierRepository repository;
    private final Clock clock;

    public SupplierServiceImpl(SupplierRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public Supplier create(CreateSupplierCommand command) {
        Supplier supplier = new Supplier(
                repository.nextId(),
                command.name(),
                command.taxId(),
                command.email(),
                "Creado vía API el " + LocalDateTime.now(clock));
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
