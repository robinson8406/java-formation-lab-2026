package com.indra.catalog.suppliers.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
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
import com.indra.catalog.suppliers.domain.InvalidSupplierException;
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

    private static final Supplier ACME =
            new Supplier("SUP-001", "ACME LTDA", "900123456-7", "compras@acme.co", "nota interna confidencial");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierService supplierService;

    @Test
    void getById_returns200WithSupplierAndHidesInternalNotes() throws Exception {
        when(supplierService.findById("SUP-001")).thenReturn(ACME);

        mockMvc.perform(get("/api/suppliers/SUP-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("SUP-001"))
                .andExpect(jsonPath("$.name").value("ACME LTDA"))
                .andExpect(jsonPath("$.taxId").value("900123456-7"))
                .andExpect(jsonPath("$.email").value("compras@acme.co"))
                .andExpect(jsonPath("$.internalNotes").doesNotExist());
    }

    @Test
    void getById_whenSupplierDoesNotExist_returns404WithErrorCode() throws Exception {
        when(supplierService.findById("SUP-999")).thenThrow(new SupplierNotFoundException("SUP-999"));

        mockMvc.perform(get("/api/suppliers/SUP-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUPPLIER_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/suppliers/SUP-999"));
    }

    @Test
    void create_delegatesRawDataToServiceAndReturns201WithLocation() throws Exception {
        CreateSupplierCommand expectedCommand = new CreateSupplierCommand("  acme ltda ", "900.123.456-7", "compras@acme.co");
        when(supplierService.create(expectedCommand)).thenReturn(ACME);

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
                .andExpect(jsonPath("$.id").value("SUP-001"))
                .andExpect(jsonPath("$.name").value("ACME LTDA"))
                .andExpect(jsonPath("$.taxId").value("900123456-7"));

        // La normalización es responsabilidad del dominio (ver SupplierTest): el controller no la aplica
        verify(supplierService).create(expectedCommand);
    }

    @Test
    void create_withBlankName_returns400AndDoesNotCallService() throws Exception {
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
                .andExpect(jsonPath("$.errors", hasItem(containsString("name"))));

        verifyNoInteractions(supplierService);
    }

    @Test
    void create_withEmptyBody_returns400AndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        verifyNoInteractions(supplierService);
    }

    @Test
    void create_whenDomainRejectsSupplier_returns400() throws Exception {
        when(supplierService.create(any())).thenThrow(new InvalidSupplierException("el NIT debe tener el formato 900123456-7"));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "ACME", "taxId": "900.123.456-7", "email": "compras@acme.co"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void delete_existingSupplier_returns204() throws Exception {
        mockMvc.perform(delete("/api/suppliers/SUP-001"))
                .andExpect(status().isNoContent());

        verify(supplierService).delete("SUP-001");
    }

    @Test
    void unexpectedError_returns500WithoutLeakingInternalDetails() throws Exception {
        when(supplierService.findById("SUP-001"))
                .thenThrow(new IllegalStateException("jdbc:postgresql://db-prod:5432 password=secreto"));

        mockMvc.perform(get("/api/suppliers/SUP-001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("password"))));
    }
    
}
