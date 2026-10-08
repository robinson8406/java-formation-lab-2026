package com.indra.catalog.products.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 120, message = "el nombre no puede superar 120 caracteres")
        String name,
        @NotNull(message = "el precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "el precio debe ser mayor que 0")
        BigDecimal price,
        @NotNull(message = "el stock es obligatorio")
        @Min(value = 0, message = "el stock no puede ser negativo")
        Integer stock) {
}
