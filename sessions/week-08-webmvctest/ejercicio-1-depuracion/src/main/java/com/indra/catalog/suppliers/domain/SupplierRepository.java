package com.indra.catalog.suppliers.domain;

import java.util.Optional;

public interface SupplierRepository {

    Supplier save(Supplier supplier);

    Optional<Supplier> findById(String id);

    boolean existsById(String id);

    void deleteById(String id);

    String nextId();

}
