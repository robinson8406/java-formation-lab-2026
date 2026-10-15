package com.indra.catalog.products.application;

import java.math.BigDecimal;

/** Datos de entrada del caso de uso; desacopla la capa de aplicación de los DTO web. */
public record CreateProductCommand(String name, BigDecimal price, int stock) {
}
