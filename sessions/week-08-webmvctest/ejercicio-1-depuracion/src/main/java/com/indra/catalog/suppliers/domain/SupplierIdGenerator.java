package com.indra.catalog.suppliers.domain;

/** Puerto de salida: cómo se generan los ids es un detalle de infraestructura. */
public interface SupplierIdGenerator {

    String nextId();
}
