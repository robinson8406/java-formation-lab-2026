package com.indra.retail.orders.web;

import java.time.Instant;
import java.util.List;

public record ErrorMessage(
    Instant timestamp,
	int status,	
	List<String> errors	
) {
}
