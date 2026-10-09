package com.indra.catalog.suppliers.web;

import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.domain.SupplierNotFoundException;
import com.indra.catalog.suppliers.domain.SupplierService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(SupplierController.class)
@Import(SupplierWebMapper.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierService supplierService;

    @Test
    @DisplayName("GET /api/suppliers/{id} debe retornar 200 OK con los datos del proveedor si existe")
    void findById_whenSupplierExists_returns200AndSupplier() throws Exception {
        Supplier supplier = new Supplier();
        supplier.setId("SUP-001");
        supplier.setName("ACME LTDA");
        supplier.setTaxId("900123456-7");
        supplier.setEmail("compras@acme.co");
        when(supplierService.findById("SUP-001")).thenReturn(supplier);

        mockMvc.perform(get("/api/suppliers/SUP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("SUP-001"))
                .andExpect(jsonPath("$.name").value("ACME LTDA"))
                .andExpect(jsonPath("$.taxId").value("900123456-7"))
                .andExpect(jsonPath("$.email").value("compras@acme.co"));

        verify(supplierService).findById("SUP-001");
    }

    @Test
    @DisplayName("GET /api/suppliers/{id} debe retornar 404 Not Found cuando el proveedor no existe")
    void findById_whenSupplierDoesNotExist_returns404() throws Exception {
        when(supplierService.findById("SUP-999")).thenThrow(new SupplierNotFoundException("SUP-999"));

        mockMvc.perform(get("/api/suppliers/SUP-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUPPLIER_NOT_FOUND"));

        verify(supplierService).findById("SUP-999");
    }

    @Test
    @DisplayName("POST /api/suppliers debe retornar 201 Created y header Location cuando el request es válido")
    void create_whenRequestIsValid_returns201AndLocationHeader() throws Exception {
        when(supplierService.create(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            supplier.setId("SUP-001");
            supplier.setName("ACME LTDA");
            supplier.setTaxId("900123456-7");
            return supplier;
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

        verify(supplierService).create(any(Supplier.class));
    }

    @Test
    @DisplayName("POST /api/suppliers debe retornar 400 Bad Request cuando la validación falla")
    void create_whenRequestIsInvalid_returns400AndValidationErrors() throws Exception {
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
    @DisplayName("DELETE /api/suppliers/{id} debe retornar 204 No Content cuando la eliminación es exitosa")
    void delete_whenSupplierExists_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/suppliers/SUP-001"))
                .andExpect(status().isNoContent());

        verify(supplierService).delete("SUP-001");
    }
}
