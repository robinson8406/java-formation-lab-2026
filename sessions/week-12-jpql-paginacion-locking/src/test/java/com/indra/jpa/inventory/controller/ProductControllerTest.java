package com.indra.jpa.inventory.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("Busqueda de productos sin nombre retorna productos paginados y total")
    void searchWithoutNameReturnsPaginatedProductsAndTotal() throws Exception {
     
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(Math.min(10, 50))))
                .andExpect(jsonPath("$.totalElements").value(50));
    }

    @Test
    @DisplayName("Busqueda de productos filtra por precio minimo y maximo")
    void searchFiltersByPriceRange() throws Exception {
        mockMvc.perform(get("/products")
                .param("minPrice", "100")
                .param("maxPrice", "200")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].price",
                        everyItem(allOf(greaterThanOrEqualTo(100.0), lessThanOrEqualTo(200.0)))));
    }



}