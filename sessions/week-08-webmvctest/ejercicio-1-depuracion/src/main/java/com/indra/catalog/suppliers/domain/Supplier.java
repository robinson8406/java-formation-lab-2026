package com.indra.catalog.suppliers.domain;

import java.util.Objects;

/**
 * Proveedor del catálogo. Inmutable: las invariantes y la normalización exigida por Compras
 * (razón social en mayúsculas, NIT sin puntos) se aplican una sola vez al construirlo.
 */
public record Supplier(String id, String name, String taxId, String email, String internalNotes) {

    public Supplier {
        Objects.requireNonNull(id, "id");
        name = requireText(name, "La razón social es obligatoria").toUpperCase();
        taxId = requireText(taxId, "El NIT es obligatorio").replace(".", "");
        email = requireText(email, "El correo es obligatorio");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
