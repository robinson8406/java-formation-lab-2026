package com.indra.jpa.inventory.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private ProductService productService;

    @Test
    @DisplayName("Busqueda de productos sin nombre retorna productos paginados y total")
    void searchWithoutNameReturnsPaginatedProductsAndTotal() throws Exception {
     
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(50));
    }

    @Test
    @DisplayName("Busqueda acepta un nombre de 200 caracteres")
    void searchAcceptsNameAtMaximumLength() throws Exception {
        String name = "x".repeat(200);
        doReturn(Page.empty()).when(productService)
            .searchProducts(eq(name), org.mockito.ArgumentMatchers.any(Pageable.class));

        mockMvc.perform(get("/products").param("name", name))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Busqueda rechaza un nombre de 201 caracteres")
    void searchRejectsNameAboveMaximumLength() throws Exception {
        mockMvc.perform(get("/products").param("name", "x".repeat(201)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }


    @ParameterizedTest
    @CsvSource({"stock,200", "reservations,204"})
    @DisplayName("Los endpoints aceptan la cantidad maxima")
    void stockEndpointsAcceptMaximumQuantity(String endpoint, Integer expectedStatus) throws Exception {
        doReturn(new Product()).when(productService).increaseStock(1L, 999999);
        doNothing().when(productService).reserveStock(1L, 999999);

        mockMvc.perform(post("/products/1/{endpoint}", endpoint).param("quantity", "999999"))
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest
    @CsvSource({
            "stock,1000000", "stock,2147483647", "stock,2147483648", "stock,0", "stock,-1",
            "reservations,1000000", "reservations,2147483647", "reservations,2147483648",
            "reservations,0", "reservations,-1",
            "stock,missing", "reservations,missing",
            "stock,not-a-number", "reservations,not-a-number"
    })
    @DisplayName("Los endpoints rechazan cantidades fuera de rango, ausentes o no numericas")
    void stockEndpointsRejectInvalidQuantities(String endpoint, String quantity) throws Exception {
        var request = post("/products/1/{endpoint}", endpoint);
        if (!"missing".equals(quantity)) {
            request.param("quantity", quantity);
        }

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }


}