package com.indra.catalog.products.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.indra.catalog.products.application.CreateProductCommand;
import com.indra.catalog.products.application.ProductService;
import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.infrastructure.security.SecurityConfig;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    private static final String PRODUCTS_URL = "/api/products";
    private static final RequestPostProcessor EDITOR = httpBasic("editor", "editor-local");
    private static final RequestPostProcessor ADMIN = httpBasic("admin", "admin-local");
    private static final Product LAPTOP = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Nested
    @DisplayName("GET /api/products/{id}")
    class FindById {

        @Test
        void returns200WithProductWhenItExists() throws Exception {
            when(productService.findById("PRD-0001")).thenReturn(LAPTOP);

            mockMvc.perform(get(PRODUCTS_URL + "/{id}", "PRD-0001"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value("PRD-0001"))
                    .andExpect(jsonPath("$.name").value("Laptop"))
                    .andExpect(jsonPath("$.price").value(3500000.00))
                    .andExpect(jsonPath("$.stock").value(10));
        }

        @Test
        void returns404WithErrorBodyWhenProductDoesNotExist() throws Exception {
            when(productService.findById("PRD-9999")).thenThrow(new ProductNotFoundException("PRD-9999"));

            mockMvc.perform(get(PRODUCTS_URL + "/{id}", "PRD-9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/api/products/PRD-9999"));
        }
    }

    @Nested
    @DisplayName("POST /api/products")
    class Create {

        @Test
        void returns201WithLocationHeaderWhenRequestIsValid() throws Exception {
            when(productService.create(any(CreateProductCommand.class))).thenReturn(LAPTOP);

            mockMvc.perform(post(PRODUCTS_URL).with(EDITOR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequestBody()))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "http://localhost/api/products/PRD-0001"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value("PRD-0001"));

            verify(productService).create(new CreateProductCommand("Laptop", new BigDecimal("3500000.00"), 10));
        }

        @Test
        void returns400WithValidationErrorsWhenNameIsBlank() throws Exception {
            mockMvc.perform(post(PRODUCTS_URL).with(EDITOR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "", "price": 3500000.00, "stock": 10}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors", hasItem(startsWith("name:"))));

            verifyNoInteractions(productService);
        }

        @Test
        void returns401WhenRequestIsAnonymous() throws Exception {
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequestBody()))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(productService);
        }

        private static String validRequestBody() {
            return """
                    {"name": "Laptop", "price": 3500000.00, "stock": 10}
                    """;
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class Delete {

        @Test
        void returns204WhenAdminDeletesExistingProduct() throws Exception {
            mockMvc.perform(delete(PRODUCTS_URL + "/{id}", "PRD-0001").with(ADMIN))
                    .andExpect(status().isNoContent());

            verify(productService).delete("PRD-0001");
        }

        @Test
        void returns404WhenProductDoesNotExist() throws Exception {
            doThrow(new ProductNotFoundException("PRD-9999")).when(productService).delete("PRD-9999");

            mockMvc.perform(delete(PRODUCTS_URL + "/{id}", "PRD-9999").with(ADMIN))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        void returns403WhenUserIsNotAdmin() throws Exception {
            mockMvc.perform(delete(PRODUCTS_URL + "/{id}", "PRD-0001").with(EDITOR))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(productService);
        }
    }
}
