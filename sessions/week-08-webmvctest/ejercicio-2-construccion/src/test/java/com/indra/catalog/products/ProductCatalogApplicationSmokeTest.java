package com.indra.catalog.products;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Único {@code @SpringBootTest} del ejercicio: smoke test que valida el cableado completo
 * (controller + service + repositorio en memoria + seguridad real). Los casos de borde
 * (validación, 404, 401/403) ya están cubiertos en niveles más bajos y rápidos.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductCatalogApplicationSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void creaUnProductoDeExtremoAExtremoConTodasLasCapasReales() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "price": 3500000.00,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
