package com.indra.catalog.suppliers.application;

import com.indra.catalog.suppliers.domain.Supplier;

public interface SupplierService {

    Supplier create(Supplier supplier);

    Supplier findById(String id);

    void delete(String id);
}
