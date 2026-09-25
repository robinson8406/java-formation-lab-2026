package com.indra.retail.orders.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    int status,
    List<String> errors
) {
    public ErrorResponse(int status, List<String> errors) {
        this(LocalDateTime.now(), status, errors);
    }

    public ErrorResponse(int status, String error) {
        this(LocalDateTime.now(), status, List.of(error));
    }

    public static ErrorResponse of(int status, List<String> errors) {
        return new ErrorResponse(status, errors);
    }

    public static ErrorResponse of(int status, String error) {
        return new ErrorResponse(status, error);
    }
}
