package com.indra.retail.onboarding.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    private String name;

    private Integer stock;

    protected Product() {
    }

    public Product(String sku, String name, Integer stock) {
        this.sku = sku;
        this.name = name;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public Integer getStock() {
        return stock;
    }
}
