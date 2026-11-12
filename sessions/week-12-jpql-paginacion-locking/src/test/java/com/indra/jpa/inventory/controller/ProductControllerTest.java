package com.indra.jpa.inventory.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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

    @ParameterizedTest
    @CsvSource(value = {
            "KeYbOaRd,NULL,NULL,10",
            "NULL,986.22,NULL,1",
            "NULL,NULL,47.31,1",
            "KeYbOaRd,127.64,137.55,3",
            "mouse,47.31,47.31,1",
            "missing,NULL,NULL,0"
    }, nullValues = "NULL")
    void searchCombinesOptionalFilters(String name, String minPrice, String maxPrice, int expectedTotal)
            throws Exception {
        MockHttpServletRequestBuilder request = get("/products")
                .param("size", "2").param("sort", "price,asc");
        if (name != null) {
            request.param("name", name);
        }
        if (minPrice != null) {
            request.param("minPrice", minPrice);
        }
        if (maxPrice != null) {
            request.param("maxPrice", maxPrice);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(Math.min(2, expectedTotal))))
                .andExpect(jsonPath("$.totalElements").value(expectedTotal));
    }

    @Test
    void filteredSearchPreservesPageAndSort() throws Exception {
        mockMvc.perform(get("/products").param("name", "keyboard")
                .param("minPrice", "127.64").param("maxPrice", "137.55")
                .param("size", "2").param("page", "1").param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].price").value(137.55))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

}