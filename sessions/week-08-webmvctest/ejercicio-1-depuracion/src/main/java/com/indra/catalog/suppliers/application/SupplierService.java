package com.indra.catalog.suppliers.application;

import com.indra.catalog.suppliers.domain.Supplier;

/** Puerto de entrada: el controller depende de esta abstracción, no de la implementación. */
public interface SupplierService {

    Supplier create(CreateSupplierCommand command);

    Supplier findById(String id);

    void delete(String id);
}
