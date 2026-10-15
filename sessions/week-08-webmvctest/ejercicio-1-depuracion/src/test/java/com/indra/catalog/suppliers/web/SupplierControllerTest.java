package com.indra.catalog.suppliers.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.indra.catalog.suppliers.application.SupplierService;
import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SupplierController.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierService supplierService;

    @Test
    void findByIdReturnsSupplierAsJson() throws Exception {
        Supplier supplier = new Supplier("SUP-001", "ACME LTDA", "900123456-7", "compras@acme.co", null);
        when(supplierService.findById("SUP-001")).thenReturn(supplier);

        mockMvc.perform(get("/api/suppliers/SUP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("SUP-001"))
                .andExpect(jsonPath("$.name").value("ACME LTDA"))
                .andExpect(jsonPath("$.taxId").value("900123456-7"))
                .andExpect(jsonPath("$.email").value("compras@acme.co"));
    }

    @Test
    void findByIdReturnsNotFoundWhenSupplierDoesNotExist() throws Exception {
        when(supplierService.findById("SUP-999")).thenThrow(new SupplierNotFoundException("SUP-999"));

        mockMvc.perform(get("/api/suppliers/SUP-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUPPLIER_NOT_FOUND"));
    }

    @Test
    void createNormalizesSupplierAndReturnsLocation() throws Exception {
        when(supplierService.create(any())).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            return supplier.withId("SUP-001");
        });

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "  acme ltda ",
                                    "taxId": "900.123.456-7",
                                    "email": "compras@acme.co"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/suppliers/SUP-001"))
                .andExpect(jsonPath("$.name").value("ACME LTDA"))
                .andExpect(jsonPath("$.taxId").value("900123456-7"));

        verify(supplierService).create(new Supplier(null, "  acme ltda ", "900.123.456-7", "compras@acme.co", null));
    }

    @Test
    void createReturnsBadRequestWhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "taxId": "900.123.456-7",
                                    "email": "compras@acme.co"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value(containsString("name")));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/suppliers/SUP-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unexpectedErrorsDoNotExposeInternalDetails() throws Exception {
        when(supplierService.findById("SUP-500")).thenThrow(new IllegalStateException("database password"));

        mockMvc.perform(get("/api/suppliers/SUP-500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value("Ocurrió un error interno"))
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.not(containsString("password"))));
    }
}
