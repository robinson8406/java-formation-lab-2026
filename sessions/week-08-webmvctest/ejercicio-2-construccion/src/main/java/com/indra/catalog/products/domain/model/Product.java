package com.indra.catalog.products.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Product {

    private final String id;
    private final String name;
    private final BigDecimal price;
    private final int stock;

    public Product(String id, String name, BigDecimal price, Integer stock) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede ser nulo ni estar vacío");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 120) {
            throw new IllegalArgumentException("El nombre del producto no puede superar los 120 caracteres");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor que 0");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("El stock del producto debe ser mayor o igual que 0");
        }

        this.id = id;
        this.name = trimmedName;
        this.price = price;
        this.stock = stock;
    }

    public Product withId(String newId) {
        return new Product(newId, this.name, this.price, this.stock);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product product = (Product) o;
        return stock == product.stock
                && Objects.equals(id, product.id)
                && Objects.equals(name, product.name)
                && Objects.equals(price, product.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, stock);
    }

    @Override
    public String toString() {
        return "Product{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", price=" + price
                + ", stock=" + stock
                + '}';
    }
}
