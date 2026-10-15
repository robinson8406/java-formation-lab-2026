package com.indra.catalog.products.web.dto;

import com.indra.catalog.products.application.CreateProductCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 120, message = "el nombre no puede superar 120 caracteres")
        String name,
        @NotNull(message = "el precio es obligatorio")
        @DecimalMin(value = "0.01", message = "el precio debe ser mayor que cero")
        BigDecimal price,
        @NotNull(message = "el stock es obligatorio")
        @PositiveOrZero(message = "el stock no puede ser negativo")
        Integer stock) {

    public CreateProductCommand toCommand() {
        return new CreateProductCommand(name, price, stock);
    }
}
