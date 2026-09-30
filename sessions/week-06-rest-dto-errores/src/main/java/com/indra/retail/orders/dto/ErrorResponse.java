package com.indra.retail.orders.dto;

import java.util.List;

public record ErrorResponse(
    String timestamp,
    int status,
    List<String> errors
) {}
