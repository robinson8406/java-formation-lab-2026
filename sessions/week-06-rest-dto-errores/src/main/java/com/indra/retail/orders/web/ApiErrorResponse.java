package com.indra.retail.orders.web;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(Instant timestamp, int status, List<String> errors) {

    public ApiErrorResponse {
        errors = List.copyOf(errors);
    }
}