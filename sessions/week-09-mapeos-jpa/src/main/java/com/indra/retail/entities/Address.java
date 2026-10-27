package com.indra.retail.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Address {

    @Column(nullable = false, length = 200)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String country;

    protected Address() {
    }

    public Address(String street, String city, String postalCode, String country) {
        this.street = Objects.requireNonNull(street, "street is required");
        this.city = Objects.requireNonNull(city, "city is required");
        this.postalCode = Objects.requireNonNull(postalCode, "postalCode is required");
        this.country = Objects.requireNonNull(country, "country is required");
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }
}
