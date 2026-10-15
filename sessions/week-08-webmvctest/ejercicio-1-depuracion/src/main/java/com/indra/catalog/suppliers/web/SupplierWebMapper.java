package com.indra.catalog.suppliers.web;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.web.dto.CreateSupplierRequest;
import com.indra.catalog.suppliers.web.dto.SupplierResponse;

/**
 * Mapeo puro entre DTO web y dominio. No es un bean: @WebMvcTest no carga @Component normales,
 * así que el controller no debe depender de uno para arrancar el slice.
 */
final class SupplierWebMapper {

    private SupplierWebMapper() {
    }

    static Supplier toDomain(CreateSupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setTaxId(request.taxId());
        supplier.setEmail(request.email());
        return supplier;
    }

    static SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.getTaxId(), supplier.getEmail());
    }
}
