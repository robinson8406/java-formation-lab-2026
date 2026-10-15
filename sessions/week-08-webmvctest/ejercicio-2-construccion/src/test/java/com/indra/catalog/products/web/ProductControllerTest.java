package com.indra.catalog.products.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ProductControllerTest {

    private static final BigDecimal PRICE = new BigDecimal("3500000.00");
    private static final Product LAPTOP = new Product("PRD-0001", "Laptop", PRICE, 10);
    private static final String VALID_BODY = """
            { "name": "Laptop", "price": 3500000.00, "stock": 10 }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    // ---------- GET ----------

    @Test
    void getById_whenProductExists_returns200WithProductJson() throws Exception {
        when(productService.findById("PRD-0001")).thenReturn(LAPTOP);

        mockMvc.perform(get("/api/products/PRD-0001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("PRD-0001"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(3500000.00))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void getById_whenProductDoesNotExist_returns404WithErrorBody() throws Exception {
        when(productService.findById("PRD-9999")).thenThrow(new ProductNotFoundException("PRD-9999"));

        mockMvc.perform(get("/api/products/PRD-9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0]").value("Producto no encontrado: PRD-9999"))
                .andExpect(jsonPath("$.path").value("/api/products/PRD-9999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ---------- POST ----------

    @Test
    @WithMockUser(roles = "EDITOR")
    void create_withValidBody_returns201WithLocationAndBody() throws Exception {
        when(productService.create(new CreateProductCommand("Laptop", PRICE, 10))).thenReturn(LAPTOP);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/PRD-0001"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("PRD-0001"))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void create_withBlankName_returns400AndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "", "price": 3500000.00, "stock": 10 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors", hasItem(containsString("name"))));

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void create_withMissingPriceAndNegativeStock_returns400WithAllErrors() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Laptop", "stock": -1 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("price"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("stock"))));

        verifyNoInteractions(productService);
    }

    // ---------- DELETE ----------

    @Test
    @WithMockUser(roles = {"EDITOR", "ADMIN"})
    void delete_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/PRD-0001"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService).delete("PRD-0001");
    }

    @Test
    @WithMockUser(roles = {"EDITOR", "ADMIN"})
    void delete_whenProductDoesNotExist_returns404() throws Exception {
        doThrow(new ProductNotFoundException("PRD-9999")).when(productService).delete("PRD-9999");

        mockMvc.perform(delete("/api/products/PRD-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    // ---------- Seguridad ----------

    @Test
    void create_anonymous_returns401AndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));

        verifyNoInteractions(productService);
    }

    @Test
    void create_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(httpBasic("editor", "clave-incorrecta"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void delete_asEditor_returns403AndDoesNotCallService() throws Exception {
        mockMvc.perform(delete("/api/products/PRD-0001"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    @Test
    void delete_anonymous_returns401() throws Exception {
        mockMvc.perform(delete("/api/products/PRD-0001"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void unexpectedError_returns500WithoutLeakingInternalDetails() throws Exception {
        when(productService.findById(any())).thenThrow(new IllegalStateException("password=secreto"));

        mockMvc.perform(get("/api/products/PRD-0001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value("ocurrió un error inesperado"));
    }
}
