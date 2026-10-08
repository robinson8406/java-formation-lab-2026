package com.indra.catalog.products.web.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(Instant timestamp, int status, String code, List<String> errors, String path) {
}
