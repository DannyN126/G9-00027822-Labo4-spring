package com.server.app.dto.finance.request;

import com.server.app.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountCreateDto {

    @NotBlank(message = "El alias de la cuenta es obligatorio")
    @Size(
            min = 2,
            max = 100,
            message = "El alias debe tener entre 2 y 100 caracteres"
    )
    private String alias;

    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(
            regexp = "^[A-Za-z]{3}$",
            message = "La moneda debe tener exactamente 3 letras, por ejemplo USD"
    )
    private String currency;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private AccountType type;
}