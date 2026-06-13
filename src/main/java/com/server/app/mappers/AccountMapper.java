package com.server.app.mappers;

import org.springframework.stereotype.Component;

import com.server.app.dto.finance.response.AccountResponse;
import com.server.app.entities.Account;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAlias(),
                account.getCurrency(),
                account.getBaseBalance(),
                account.getType()
        );
    }
}