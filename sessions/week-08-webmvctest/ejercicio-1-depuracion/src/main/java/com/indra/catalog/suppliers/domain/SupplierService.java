package com.indra.catalog.suppliers.domain;

public interface SupplierService {

    Supplier findById(String id);

    Supplier create(Supplier supplier);

    void delete(String id);
}
