package com.server.app.services;

import com.server.app.dto.finance.request.AccountCreateDto;
import com.server.app.dto.finance.response.AccountResponse;
import com.server.app.entities.Account;
import com.server.app.entities.User;
import com.server.app.exceptions.ConfictException;
import com.server.app.mappers.AccountMapper;
import com.server.app.repositories.AccountRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public Page<AccountResponse> findAllByUser(
            User user,
            int page,
            int size
    ) {

        validatePagination(page, size);
        return accountRepository
                .findAllByUserId(
                        user.getId(),
                        PageRequest.of(page, size)
                )
                .map(accountMapper::toResponse);
    }

    @Transactional
    public AccountResponse create(
            User user,
            AccountCreateDto dto
    ) {
        String alias = dto.getAlias().trim();

        boolean aliasExists =
                accountRepository
                        .existsByAliasIgnoreCaseAndUserId(
                                alias,
                                user.getId()
                        );

        if (aliasExists) {
            throw new ConfictException(
                    "Ya existe una cuenta con ese alias"
            );
        }

        Account account = Account.builder()
                .alias(alias)
                .currency(dto.getCurrency().trim().toUpperCase())
                .type(dto.getType())
                .baseBalance(BigDecimal.ZERO)
                .user(user)
                .build();

        return accountMapper.toResponse(
                accountRepository.save(account)
        );
    }

    @Transactional(readOnly = true)
    public Account findEntityByIdAndUser(
            Long accountId,
            Integer userId
    ) {
        return accountRepository
                .findByIdAndUserId(accountId, userId)
                .orElseThrow(() ->
                        new ConfictException(
                                "La cuenta no existe o no pertenece al usuario"
                        )
                );
    }
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "El número de página no puede ser negativo"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe estar entre 1 y 100"
            );
        }
    }
}