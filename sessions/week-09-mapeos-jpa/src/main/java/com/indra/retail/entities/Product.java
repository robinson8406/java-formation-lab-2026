package com.indra.retail.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Product() {
    }

    public Product(String name, BigDecimal price, Integer stock) {
        this.name = Objects.requireNonNull(name, "name is required");
        this.price = Objects.requireNonNull(price, "price is required");
        this.stock = Objects.requireNonNull(stock, "stock is required");
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock cannot be negative");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public Long getVersion() {
        return version;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
