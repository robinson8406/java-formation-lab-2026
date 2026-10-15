package com.indra.catalog.suppliers.web;

import com.indra.catalog.suppliers.application.SupplierService;
import com.indra.catalog.suppliers.web.dto.CreateSupplierRequest;
import com.indra.catalog.suppliers.web.dto.SupplierResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Solo traduce HTTP ↔ caso de uso: las reglas de negocio viven en el dominio y en el servicio. */
@RestController
@RequestMapping(path = "/api/suppliers", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable String id) {
        return SupplierResponse.from(supplierService.findById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse created = SupplierResponse.from(supplierService.create(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
