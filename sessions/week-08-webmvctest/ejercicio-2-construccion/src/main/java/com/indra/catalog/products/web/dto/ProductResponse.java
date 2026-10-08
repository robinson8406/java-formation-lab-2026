package com.indra.catalog.products.web.dto;

import java.math.BigDecimal;

public record ProductResponse(String id, String name, BigDecimal price, int stock) {
}
