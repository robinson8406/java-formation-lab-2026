package com.indra.catalog.products;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Único {@code @SpringBootTest} del módulo: valida que todas las capas reales queden cableadas
 * (controller → servicio → repositorio en memoria + seguridad con usuarios reales).
 * Los casos de borde se prueban en niveles más bajos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductCatalogSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createGetAndDeleteProduct_throughAllLayers() throws Exception {
        String body = mockMvc.perform(post("/api/products")
                        .with(httpBasic("editor", "editor-test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Laptop", "price": 3500000.00, "stock": 10 }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));

        mockMvc.perform(delete("/api/products/{id}", id).with(httpBasic("admin", "admin-test")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }
}
