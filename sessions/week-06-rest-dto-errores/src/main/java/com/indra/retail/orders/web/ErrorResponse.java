package com.indra.retail.orders.web;

import java.time.Instant;
import java.util.List;

/** Contrato de error único para toda la API. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        List<String> errors,
        String path) {
}
