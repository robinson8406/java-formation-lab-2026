package com.indra.catalog.suppliers.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public record Supplier(String id, String name, String taxId, String email, String internalNotes) {

    private static final Pattern TAX_ID = Pattern.compile("\\d{9,10}-\\d");

    public Supplier {
        requireText(id, "el id es obligatorio");
        requireText(name, "la razón social es obligatoria");
        requireText(taxId, "el NIT es obligatorio");
        requireText(email, "el correo es obligatorio");

        // Normalización exigida por el área de compras: razón social en mayúsculas y NIT sin puntos
        name = name.trim().toUpperCase(Locale.ROOT);
        taxId = taxId.replace(".", "").trim();
        email = email.trim();
        if (!TAX_ID.matcher(taxId).matches()) {
            throw new InvalidSupplierException("el NIT debe tener el formato 900123456-7");
        }
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidSupplierException(message);
        }
    }
}

