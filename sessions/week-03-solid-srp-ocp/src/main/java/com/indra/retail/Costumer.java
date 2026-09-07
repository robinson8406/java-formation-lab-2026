package com.indra.retail;

import java.time.LocalDate;

public class Costumer {

    private final String id;
    private final String email;
    private final LocalDate customerJoinDate;

    public Costumer(String id, String email, LocalDate customerJoinDate) {
        this.id = id;
        this.email = email;
        this.customerJoinDate = customerJoinDate;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getCustomerJoinDate() {
        return customerJoinDate;
    }
}
