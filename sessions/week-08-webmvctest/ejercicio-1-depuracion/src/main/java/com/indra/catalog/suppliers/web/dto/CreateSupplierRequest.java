package com.indra.catalog.suppliers.web.dto;

import com.indra.catalog.suppliers.application.CreateSupplierCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateSupplierRequest(
        @NotBlank(message = "la razón social es obligatoria")
        String name,
        @NotBlank(message = "el NIT es obligatorio")
        @Pattern(regexp = "[0-9.]{9,11}-[0-9]", message = "el NIT debe tener el formato 900123456-7")
        String taxId,
        @NotBlank(message = "el correo es obligatorio")
        @Email(message = "el correo no es válido")
        String email) {

    public CreateSupplierCommand toCommand() {
        return new CreateSupplierCommand(name, taxId, email);
    }
}
