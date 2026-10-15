package com.indra.catalog.suppliers.web.dto;

import com.indra.catalog.suppliers.domain.Supplier;

/** No expone internalNotes: es información interna, no parte del contrato público. */
public record SupplierResponse(String id, String name, String taxId, String email) {

    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(supplier.id(), supplier.name(), supplier.taxId(), supplier.email());
    }
}
