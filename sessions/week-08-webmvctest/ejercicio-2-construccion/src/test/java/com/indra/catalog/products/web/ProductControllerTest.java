package com.indra.catalog.products.web;

import com.indra.catalog.products.application.ProductService;
import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.infrastructure.security.SecurityConfig;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    public ProductControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET /api/products/{id} publico con producto existente retorna 200 y JSON correspondiente")
    void findById_existingProduct_returns200AndProductResponse() throws Exception {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
        when(productService.findById("PRD-0001")).thenReturn(product);

        mockMvc.perform(get("/api/products/PRD-0001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("PRD-0001"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(3500000.00))
                .andExpect(jsonPath("$.stock").value(10));

        verify(productService).findById("PRD-0001");
    }

    @Test
    @DisplayName("GET /api/products/{id} con producto inexistente retorna 404 y PRODUCT_NOT_FOUND")
    void findById_nonExistingProduct_returns404AndErrorResponse() throws Exception {
        when(productService.findById("PRD-9999")).thenThrow(new ProductNotFoundException("PRD-9999"));

        mockMvc.perform(get("/api/products/PRD-9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0]").value("Producto no encontrado: PRD-9999"))
                .andExpect(jsonPath("$.path").value("/api/products/PRD-9999"));

        verify(productService).findById("PRD-9999");
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    @DisplayName("POST /api/products con rol EDITOR y datos validos retorna 201, Location y JSON creado")
    void create_validRequestWithEditorRole_returns201AndLocationHeader() throws Exception {
        Product created = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
        when(productService.create(any(Product.class))).thenReturn(created);

        String jsonBody = """
                {
                    "name": "Laptop",
                    "price": 3500000.00,
                    "stock": 10
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/PRD-0001"))
                .andExpect(jsonPath("$.id").value("PRD-0001"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(3500000.00))
                .andExpect(jsonPath("$.stock").value(10));

        verify(productService).create(any(Product.class));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    @DisplayName("POST /api/products con nombre vacio retorna 400 y VALIDATION_ERROR")
    void create_invalidRequestEmptyName_returns400AndValidationError() throws Exception {
        String jsonBody = """
                {
                    "name": "",
                    "price": 100.00,
                    "stock": 5
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors", hasItem(containsString("El nombre es obligatorio"))));
    }

    @Test
    @DisplayName("POST /api/products sin autenticacion retorna 401 Unauthorized")
    void create_anonymousUser_returns401Unauthorized() throws Exception {
        String jsonBody = """
                {
                    "name": "Laptop",
                    "price": 3500000.00,
                    "stock": 10
                }
                """;

        mockMvc.perform(post("/api/products")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/products/{id} con rol ADMIN retorna 204 No Content")
    void delete_withAdminRole_returns204NoContent() throws Exception {
        doNothing().when(productService).deleteById("PRD-0001");

        mockMvc.perform(delete("/api/products/PRD-0001"))
                .andExpect(status().isNoContent());

        verify(productService).deleteById("PRD-0001");
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    @DisplayName("DELETE /api/products/{id} con rol EDITOR retorna 403 Forbidden")
    void delete_withEditorRole_returns403Forbidden() throws Exception {
        mockMvc.perform(delete("/api/products/PRD-0001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/products/{id} con producto inexistente retorna 404")
    void delete_nonExistingProductWithAdminRole_returns404() throws Exception {
        doThrow(new ProductNotFoundException("PRD-9999")).when(productService).deleteById("PRD-9999");

        mockMvc.perform(delete("/api/products/PRD-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0]").value("Producto no encontrado: PRD-9999"));

        verify(productService).deleteById("PRD-9999");
    }

    @Test
    @DisplayName("Error inesperado en endpoint retorna 500 e INTERNAL_ERROR sin filtrar detalles internos")
    void handleUnexpected_returns500InternalError() throws Exception {
        when(productService.findById("ERR-500")).thenThrow(new RuntimeException("Fallo inesperado de base de datos"));

        mockMvc.perform(get("/api/products/ERR-500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value("Ha ocurrido un error interno en el servidor"));

        verify(productService).findById("ERR-500");
    }
}
