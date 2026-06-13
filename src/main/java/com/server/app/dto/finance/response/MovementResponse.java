package com.server.app.dto.finance.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.server.app.enums.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovementResponse(
        Long id,
        BigDecimal amount,
        String originalCurrency,
        BigDecimal exchangeRate,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime date,

        String description,
        Long accountId,
        String accountAlias,
        Long categoryId,
        String categoryName,
        CategoryType categoryType
) {
}