package com.indra.catalog.suppliers.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SupplierTest {

    @Test
    @DisplayName("Debe normalizar la razón social a mayúsculas sin espacios y eliminar puntos del NIT")
    void normalize_normalizesNameAndTaxId() {
        Supplier supplier = new Supplier();
        supplier.setName("   distribuidora del norte s.a.s.  ");
        supplier.setTaxId("  900.555.123-4 ");

        supplier.normalize();

        assertEquals("DISTRIBUIDORA DEL NORTE S.A.S.", supplier.getName());
        assertEquals("900555123-4", supplier.getTaxId());
    }

    @Test
    @DisplayName("Debe manejar valores nulos durante la normalización sin lanzar excepción")
    void normalize_handlesNullsSafely() {
        Supplier supplier = new Supplier();

        supplier.normalize();

        assertNull(supplier.getName());
        assertNull(supplier.getTaxId());
    }
}
