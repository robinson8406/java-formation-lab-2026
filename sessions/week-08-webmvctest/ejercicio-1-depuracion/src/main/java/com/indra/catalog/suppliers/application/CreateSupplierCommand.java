package com.indra.catalog.suppliers.application;

public record CreateSupplierCommand(String name, String taxId, String email) {
}
