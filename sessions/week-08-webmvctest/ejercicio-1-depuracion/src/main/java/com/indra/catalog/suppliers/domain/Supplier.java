package com.indra.catalog.suppliers.domain;

import java.time.LocalDateTime;

/**
 * Proveedor del catálogo de compras. Inmutable: protege sus invariantes (razón
 * social, NIT y correo obligatorios) y aplica la normalización de compras al registrarse.
 */
public record Supplier(String id, String name, String taxId, String email, String internalNotes) {

    public Supplier {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("La razón social del proveedor es obligatoria");
        }
        if (taxId == null || taxId.isBlank()) {
            throw new IllegalArgumentException("El NIT del proveedor es obligatorio");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo del proveedor es obligatorio");
        }
    }

    /** Registra un proveedor nuevo (sin id) normalizando razón social y NIT. */
    public static Supplier register(String name, String taxId, String email) {
        return new Supplier(null, normalizeName(name), normalizeTaxId(taxId), email,
                "Creado vía API el " + LocalDateTime.now());
    }

    public Supplier withId(String id) {
        return new Supplier(id, name, taxId, email, internalNotes);
    }

    private static String normalizeName(String name) {
        return name.trim().toUpperCase();
    }

    private static String normalizeTaxId(String taxId) {
        return taxId.replace(".", "").trim();
    }
}
