package com.indra.catalog.suppliers.web;

import com.indra.catalog.suppliers.application.CreateSupplierCommand;
import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.web.dto.CreateSupplierRequest;
import com.indra.catalog.suppliers.web.dto.SupplierResponse;


final class SupplierWebMapper {

    private SupplierWebMapper() {
    }

    static CreateSupplierCommand toCommand(CreateSupplierRequest request) {
        return new CreateSupplierCommand(request.name(), request.taxId(), request.email());
    }

    static SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(supplier.id(), supplier.name(), supplier.taxId(), supplier.email());
    }
}
