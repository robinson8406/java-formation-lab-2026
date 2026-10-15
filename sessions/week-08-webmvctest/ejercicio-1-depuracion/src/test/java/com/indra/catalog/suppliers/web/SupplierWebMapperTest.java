package com.indra.catalog.suppliers.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.web.dto.CreateSupplierRequest;
import com.indra.catalog.suppliers.web.dto.SupplierResponse;
import org.junit.jupiter.api.Test;

class SupplierWebMapperTest {

    @Test
    void mapsRequestToDomainAndDomainToResponse() {
        Supplier supplier = SupplierWebMapper.toDomain(
                new CreateSupplierRequest("Acme Ltda", "900123456-7", "compras@acme.co"));

        assertEquals(new SupplierResponse(null, "ACME LTDA", "900123456-7", "compras@acme.co"),
                SupplierWebMapper.toResponse(supplier));
    }
}
