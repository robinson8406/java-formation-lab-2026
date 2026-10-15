package com.indra.catalog.suppliers.infrastructure;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
public class InMemorySupplierRepository implements SupplierRepository {

    private final Map<String, Supplier> suppliers = new ConcurrentHashMap<>();

    @Override
    public Supplier save(Supplier supplier) {
        suppliers.put(supplier.id(), supplier);
        return supplier;
    }

    @Override
    public Optional<Supplier> findById(String id) {
        return Optional.ofNullable(suppliers.get(id));
    }

    @Override
    public boolean deleteById(String id) {
        return suppliers.remove(id) != null;
    }
}
