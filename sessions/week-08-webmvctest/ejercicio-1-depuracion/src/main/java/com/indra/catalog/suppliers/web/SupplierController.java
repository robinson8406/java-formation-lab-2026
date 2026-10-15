package com.indra.catalog.suppliers.web;

import com.indra.catalog.suppliers.application.SupplierServiceImpl;
import com.indra.catalog.suppliers.domain.Supplier;
import com.indra.catalog.suppliers.web.dto.CreateSupplierRequest;
import com.indra.catalog.suppliers.web.dto.SupplierResponse;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierServiceImpl supplierService;

    public SupplierController(SupplierServiceImpl supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(SupplierWebMapper.toResponse(supplierService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@RequestBody CreateSupplierRequest request) {
        Supplier supplier = SupplierWebMapper.toDomain(request);
        // Normalización exigida por el área de compras: razón social en mayúsculas y NIT sin puntos
        supplier.setName(supplier.getName().trim().toUpperCase());
        supplier.setTaxId(supplier.getTaxId().replace(".", "").trim());
        supplier.setInternalNotes("Creado vía API el " + LocalDateTime.now());

        Supplier created = supplierService.create(supplier);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(SupplierWebMapper.toResponse(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        supplierService.delete(id);
        return ResponseEntity.ok().build();
    }
}
