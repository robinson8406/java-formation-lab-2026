package com.indra.catalog.products.application;

import java.math.BigDecimal;

public record CreateProductCommand(String name, BigDecimal price, int stock) {
    
}
