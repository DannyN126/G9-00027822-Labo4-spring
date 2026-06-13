package com.server.app.dto.finance.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long sourceMovementId,
        Long destinationMovementId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal sourceAmount,
        BigDecimal destinationAmount,
        String sourceCurrency,
        String destinationCurrency,
        BigDecimal exchangeRate,
        LocalDateTime date,
        String description
) {
}