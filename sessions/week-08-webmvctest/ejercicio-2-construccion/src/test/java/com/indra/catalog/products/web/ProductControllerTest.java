package com.indra.catalog.products.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test: solo levanta {@link ProductController}. Se importa {@link SecurityConfig}
 * explícitamente porque, con Spring Security en el classpath, @WebMvcTest aplica por
 * defecto su propia autoconfiguración de seguridad en vez de la del proyecto.
 */
@WebMvcTest(ProductController.class)
@ContextConfiguration(classes = {ProductController.class, ApiExceptionHandler.class})
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void findById_conProductoExistente_devuelve200ConJsonPathYContentType() throws Exception {
        Product product = new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
        when(productService.findById("PRD-0001")).thenReturn(product);

        mockMvc.perform(get("/api/products/PRD-0001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("PRD-0001"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(3500000.00))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void findById_conProductoInexistente_devuelve404() throws Exception {
        when(productService.findById("PRD-9999")).thenThrow(new ProductNotFoundException("PRD-9999"));

        mockMvc.perform(get("/api/products/PRD-9999"))
                .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void create_conBodyValidoYRolEditor_devuelve201ConLocationYContentType() throws Exception {
        when(productService.create(any())).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            return product.withId("PRD-0001");
        });

        mockMvc.perform(post("/api/products")
                        .with(httpBasic("editor", "editor123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "price": 3500000.00,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", "http://localhost/api/products/PRD-0001"))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void create_conNombreVacio_devuelve400YNuncaInvocaElServicio() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(httpBasic("editor", "editor123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "price": 3500000.00,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value(containsString("name")));

        verify(productService, never()).create(any());
    }

    @Test
    void create_sinCredenciales_devuelve401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "price": 3500000.00,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""))
                .andExpect(header().doesNotExist(HttpHeaders.CONTENT_TYPE));

        verify(productService, never()).create(any());
    }

    @Test
    void delete_conRolAdmin_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/products/PRD-0001")
                        .with(httpBasic("admin", "admin123")))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))
            .andExpect(header().doesNotExist(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void delete_conRolEditorSinAdmin_devuelve403() throws Exception {
        mockMvc.perform(delete("/api/products/PRD-0001")
                        .with(httpBasic("editor", "editor123")))
            .andExpect(status().isForbidden())
            .andExpect(content().string(""))
            .andExpect(header().doesNotExist(HttpHeaders.CONTENT_TYPE));

        verify(productService, never()).delete(any());
    }
}
