package com.indra.catalog.suppliers.web;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.web.dto.CreateSupplierRequest;
import com.indra.catalog.suppliers.web.dto.SupplierResponse;

/**
 * Traductor HTTP puro: no tiene estado ni dependencias, por lo que no necesita
 * ser un bean de Spring ni cargarse en el slice {@code @WebMvcTest}.
 */
final class SupplierWebMapper {

    private SupplierWebMapper() {
    }

    static Supplier toDomain(CreateSupplierRequest request) {
        return Supplier.register(request.name(), request.taxId(), request.email());
    }

    static SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(supplier.id(), supplier.name(), supplier.taxId(), supplier.email());
    }
}
