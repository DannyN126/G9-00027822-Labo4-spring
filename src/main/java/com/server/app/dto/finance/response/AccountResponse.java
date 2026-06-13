package com.server.app.dto.finance.response;

import com.server.app.enums.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String alias,
        String currency,
        BigDecimal baseBalance,
        AccountType type
) {
}