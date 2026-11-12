package com.indra.jpa.inventory.controller;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<Product> search(@RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault() Pageable pageable) {
        return productService.searchProducts(name, minPrice, maxPrice, pageable);
    }

    @PostMapping("/{productId}/stock")
    public Product increaseStock(@PathVariable Long productId, @RequestParam("quantity") int quantity) {
        return productService.increaseStock(productId, quantity);
    }

    @PostMapping("/{productId}/reservations")
    public ResponseEntity<Void> reserve(@PathVariable Long productId,
            @RequestParam("quantity") int quantity) {
        productService.reserveStock(productId, quantity);
        return ResponseEntity.noContent().build();
    }

}