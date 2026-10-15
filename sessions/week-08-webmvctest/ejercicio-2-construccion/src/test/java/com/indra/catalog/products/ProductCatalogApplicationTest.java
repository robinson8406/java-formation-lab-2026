package com.indra.catalog.products;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.security.editor-password=test-editor-password",
        "app.security.admin-password=test-admin-password"
})
class ProductCatalogApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applicationWiresProductUseCasesAndPersistence() throws Exception {
        String location = mockMvc.perform(post("/api/products")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "test-admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Laptop","price":3500000.00,"stock":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assertNotNull(location);
        String id = location.substring(location.lastIndexOf('/') + 1);
        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(delete("/api/products/" + id)
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "test-admin-password")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/PRD-9999"))
                .andExpect(status().isNotFound());
    }
}
