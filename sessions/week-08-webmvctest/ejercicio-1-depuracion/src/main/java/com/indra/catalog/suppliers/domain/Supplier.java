package com.indra.catalog.suppliers.domain;

import java.util.Locale;
import java.util.Objects;

public record Supplier(String id, String name, String taxId, String email, String internalNotes) {

    public Supplier {
        name = Objects.requireNonNull(name, "name").strip().toUpperCase(Locale.ROOT);
        taxId = Objects.requireNonNull(taxId, "taxId").replace(".", "").strip();
        email = Objects.requireNonNull(email, "email").strip();

        if (name.isBlank() || taxId.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("Los datos del proveedor son obligatorios");
        }
        if (id != null && id.isBlank()) {
            throw new IllegalArgumentException("El identificador no puede estar vacío");
        }
    }

    public Supplier withId(String supplierId) {
        return new Supplier(supplierId, name, taxId, email, internalNotes);
    }
}
