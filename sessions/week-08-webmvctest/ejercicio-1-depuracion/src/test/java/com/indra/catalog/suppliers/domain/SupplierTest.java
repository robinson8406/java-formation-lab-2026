package com.indra.catalog.suppliers.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SupplierTest {

    @Test
    void normalizesSupplierDataAndRejectsBlankName() {
        Supplier supplier = new Supplier(null, "  acme ltda ", "900.123.456-7", " compras@acme.co ", null);

        assertEquals("ACME LTDA", supplier.name());
        assertEquals("900123456-7", supplier.taxId());
        assertEquals("compras@acme.co", supplier.email());
        assertThrows(IllegalArgumentException.class,
                () -> new Supplier(null, " ", "900123456-7", "compras@acme.co", null));
    }
}
