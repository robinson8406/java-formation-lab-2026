package com.indra.catalog.suppliers.application;

/** Datos de entrada del caso de uso; desacopla la capa de aplicación de los DTO web. */
public record CreateSupplierCommand(String name, String taxId, String email) {
}
