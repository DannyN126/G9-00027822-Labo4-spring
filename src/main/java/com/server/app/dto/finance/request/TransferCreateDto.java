package com.server.app.dto.finance.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferCreateDto {

    @NotNull(message = "La cuenta de origen es obligatoria")
    private Long sourceAccountId;

    @NotNull(message = "La cuenta de destino es obligatoria")
    private Long destinationAccountId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(
            value = "0.01",
            message = "El monto debe ser mayor que cero"
    )
    private BigDecimal amount;

    @DecimalMin(
            value = "0.00000001",
            message = "La tasa de cambio debe ser mayor que cero"
    )
    private BigDecimal exchangeRate;

    @Size(
            max = 255,
            message = "La descripción no puede superar 255 caracteres"
    )
    private String description;
}