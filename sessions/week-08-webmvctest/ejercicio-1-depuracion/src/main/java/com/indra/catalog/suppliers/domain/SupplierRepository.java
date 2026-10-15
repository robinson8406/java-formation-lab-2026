package com.indra.catalog.suppliers.domain;

import java.util.Optional;

public interface SupplierRepository {

    Supplier save(Supplier supplier);

    Optional<Supplier> findById(String id);

    boolean deleteById(String id);
}
