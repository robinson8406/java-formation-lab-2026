package com.indra.catalog.suppliers.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.indra.catalog.suppliers.application.CreateSupplierCommand;
import com.indra.catalog.suppliers.application.SupplierService;
import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test: solo prueba la traducción HTTP ↔ caso de uso. La normalización de datos se prueba en
 * {@code SupplierTest} y la generación de id en {@code DefaultSupplierServiceTest}.
 */
@WebMvcTest(SupplierController.class)
class SupplierControllerTest {

    private static final String SUPPLIERS_URL = "/api/suppliers";
    private static final Supplier ACME =
            new Supplier("SUP-001", "ACME LTDA", "900123456-7", "compras@acme.co", "nota interna");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierService supplierService;

    @Nested
    @DisplayName("GET /api/suppliers/{id}")
    class FindById {

        @Test
        void returns200WithSupplierWhenItExists() throws Exception {
            when(supplierService.findById("SUP-001")).thenReturn(ACME);

            mockMvc.perform(get(SUPPLIERS_URL + "/{id}", "SUP-001"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value("SUP-001"))
                    .andExpect(jsonPath("$.name").value("ACME LTDA"))
                    .andExpect(jsonPath("$.taxId").value("900123456-7"))
                    .andExpect(jsonPath("$.email").value("compras@acme.co"))
                    .andExpect(jsonPath("$.internalNotes").doesNotExist());
        }

        @Test
        void returns404WithErrorBodyWhenSupplierDoesNotExist() throws Exception {
            when(supplierService.findById("SUP-999")).thenThrow(new SupplierNotFoundException("SUP-999"));

            mockMvc.perform(get(SUPPLIERS_URL + "/{id}", "SUP-999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("SUPPLIER_NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/api/suppliers/SUP-999"));
        }

        @Test
        void returns500WithoutInternalDetailsWhenSomethingUnexpectedFails() throws Exception {
            when(supplierService.findById("SUP-001"))
                    .thenThrow(new IllegalStateException("jdbc:postgresql://db-interna password=secreto"));

            mockMvc.perform(get(SUPPLIERS_URL + "/{id}", "SUP-001"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.errors[0]").value("Error interno del servidor"))
                    .andExpect(content().string(not(containsString("secreto"))));
        }
    }

    @Nested
    @DisplayName("POST /api/suppliers")
    class Create {

        @Test
        void returns201WithLocationAndPassesRawDataToTheService() throws Exception {
            when(supplierService.create(any(CreateSupplierCommand.class))).thenReturn(ACME);

            mockMvc.perform(post(SUPPLIERS_URL)
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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("ACME LTDA"))
                    .andExpect(jsonPath("$.taxId").value("900123456-7"));

            // El controller no normaliza: entrega los datos tal cual al caso de uso.
            verify(supplierService).create(new CreateSupplierCommand("  acme ltda ", "900.123.456-7", "compras@acme.co"));
        }

        @Test
        void returns400WithValidationErrorsWhenNameIsBlank() throws Exception {
            mockMvc.perform(post(SUPPLIERS_URL)
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
                    .andExpect(jsonPath("$.errors", hasItem(startsWith("name:"))));

            verifyNoInteractions(supplierService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/suppliers/{id}")
    class Delete {

        @Test
        void returns204AndDeletesTheSupplier() throws Exception {
            mockMvc.perform(delete(SUPPLIERS_URL + "/{id}", "SUP-001"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(supplierService).delete("SUP-001");
        }

        @Test
        void returns404WhenSupplierDoesNotExist() throws Exception {
            doThrow(new SupplierNotFoundException("SUP-999")).when(supplierService).delete("SUP-999");

            mockMvc.perform(delete(SUPPLIERS_URL + "/{id}", "SUP-999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("SUPPLIER_NOT_FOUND"));
        }
    }
}
