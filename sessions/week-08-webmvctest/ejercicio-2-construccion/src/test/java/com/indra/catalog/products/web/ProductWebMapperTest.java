package com.indra.catalog.products.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.indra.catalog.products.application.CreateProductCommand;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.web.dto.CreateProductRequest;
import com.indra.catalog.products.web.dto.ProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductWebMapperTest {

    private static final BigDecimal PRICE = new BigDecimal("3500000.00");

    @Test
    void toCommand_copiesRequestFields() {
        CreateProductCommand command = ProductWebMapper.toCommand(new CreateProductRequest("Laptop", PRICE, 10));

        assertThat(command).isEqualTo(new CreateProductCommand("Laptop", PRICE, 10));
    }

    @Test
    void toResponse_copiesProductFields() {
        ProductResponse response = ProductWebMapper.toResponse(new Product("PRD-0001", "Laptop", PRICE, 10));

        assertThat(response).isEqualTo(new ProductResponse("PRD-0001", "Laptop", PRICE, 10));
    }
}
