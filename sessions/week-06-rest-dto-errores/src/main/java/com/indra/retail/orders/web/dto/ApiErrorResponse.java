package com.indra.retail.orders.web.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        List<String> errors) {
}
