package com.indra.catalog.products;

import com.indra.catalog.products.application.ProductService;
import com.indra.catalog.products.domain.port.ProductRepository;
import com.indra.catalog.products.web.ProductController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ProductCatalogApplicationTests {

    private final ProductController productController;
    private final ProductService productService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductCatalogApplicationTests(
            ProductController productController,
            ProductService productService,
            ProductRepository productRepository) {
        this.productController = productController;
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @Test
    @DisplayName("Debe cargar el contexto de Spring Boot y cablear todos los beans del catalogo")
    void contextLoads() {
        assertNotNull(productController);
        assertNotNull(productService);
        assertNotNull(productRepository);
    }
}
