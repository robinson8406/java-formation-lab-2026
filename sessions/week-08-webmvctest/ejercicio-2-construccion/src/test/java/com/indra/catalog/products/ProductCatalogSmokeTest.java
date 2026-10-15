package com.indra.catalog.products;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Único test de integración: valida que todas las capas quedan cableadas.
 * Los casos de borde viven en los tests unitarios y en el slice test del controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductCatalogSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createReadAndDeleteProductThroughAllLayers() throws Exception {
        String location = mockMvc.perform(post("/api/products").with(httpBasic("editor", "editor-local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Monitor", "price": 900000, "stock": 3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monitor"));

        mockMvc.perform(delete(location).with(httpBasic("admin", "admin-local")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isNotFound());
    }
}
