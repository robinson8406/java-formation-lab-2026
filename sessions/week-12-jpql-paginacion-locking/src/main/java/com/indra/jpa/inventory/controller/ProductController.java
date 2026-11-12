package com.indra.jpa.inventory.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.service.ProductService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<Product> search(@RequestParam(required = false) @Size(max = 200) String name,
            @PageableDefault(size = 10) Pageable pageable) {
        return productService.searchProducts(name, pageable);
    }

    @PostMapping("/{productId}/stock")
    public Product increaseStock(@PathVariable Long productId,
            @RequestParam("quantity") @Min(1) @Max(999999) Integer quantity) {
        return productService.increaseStock(productId, quantity);
    }

    @PostMapping("/{productId}/reservations")
    public ResponseEntity<Void> reserve(@PathVariable Long productId,
            @RequestParam("quantity") @Min(1) @Max(999999) Integer quantity) {
        productService.reserveStock(productId, quantity);
        return ResponseEntity.noContent().build();
    }

}