package com.server.app.services;

import com.server.app.dto.finance.request.TransferCreateDto;
import com.server.app.dto.finance.response.TransferResponse;
import com.server.app.entities.Account;
import com.server.app.entities.Category;
import com.server.app.entities.Movement;
import com.server.app.entities.User;
import com.server.app.enums.CategoryType;
import com.server.app.exceptions.ConfictException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.AccountRepository;
import com.server.app.repositories.CategoryRepository;
import com.server.app.repositories.MovementRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TransferService {

    private static final String OUTGOING_CATEGORY =
            "Transferencia enviada";

    private static final String INCOMING_CATEGORY =
            "Transferencia recibida";

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final MovementRepository movementRepository;

    @Transactional
    public TransferResponse transfer(
            User user,
            TransferCreateDto dto
    ) {
        validateDifferentAccounts(dto);

        /*
         * Se bloquean en orden ascendente para reducir
         * el riesgo de interbloqueo.
         */
        Long firstId = Math.min(
                dto.getSourceAccountId(),
                dto.getDestinationAccountId()
        );

        Long secondId = Math.max(
                dto.getSourceAccountId(),
                dto.getDestinationAccountId()
        );

        Account firstAccount =
                findAccountForUpdate(firstId, user.getId());

        Account secondAccount =
                findAccountForUpdate(secondId, user.getId());

        Account sourceAccount =
                firstAccount.getId()
                        .equals(dto.getSourceAccountId())
                        ? firstAccount
                        : secondAccount;

        Account destinationAccount =
                firstAccount.getId()
                        .equals(dto.getDestinationAccountId())
                        ? firstAccount
                        : secondAccount;

        BigDecimal exchangeRate =
                determineExchangeRate(
                        sourceAccount,
                        destinationAccount,
                        dto.getExchangeRate()
                );

        validateFunds(sourceAccount, dto.getAmount());

        BigDecimal destinationAmount =
                dto.getAmount().multiply(exchangeRate);

        sourceAccount.setBaseBalance(
                sourceAccount
                        .getBaseBalance()
                        .subtract(dto.getAmount())
        );

        destinationAccount.setBaseBalance(
                destinationAccount
                        .getBaseBalance()
                        .add(destinationAmount)
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Category outgoingCategory =
                findTransferCategory(
                        OUTGOING_CATEGORY,
                        CategoryType.EGRESO
                );

        Category incomingCategory =
                findTransferCategory(
                        INCOMING_CATEGORY,
                        CategoryType.INGRESO
                );

        LocalDateTime transferDate = LocalDateTime.now();

        String description =
                normalizeDescription(dto.getDescription());

        Movement sourceMovement = Movement.builder()
                .amount(dto.getAmount().negate())
                .originalCurrency(sourceAccount.getCurrency())
                .exchangeRate(exchangeRate)
                .date(transferDate)
                .description(description)
                .account(sourceAccount)
                .category(outgoingCategory)
                .build();

        Movement destinationMovement = Movement.builder()
                .amount(destinationAmount)
                .originalCurrency(sourceAccount.getCurrency())
                .exchangeRate(exchangeRate)
                .date(transferDate)
                .description(description)
                .account(destinationAccount)
                .category(incomingCategory)
                .build();

        sourceMovement =
                movementRepository.save(sourceMovement);

        destinationMovement =
                movementRepository.save(destinationMovement);

        return new TransferResponse(
                sourceMovement.getId(),
                destinationMovement.getId(),
                sourceAccount.getId(),
                destinationAccount.getId(),
                dto.getAmount(),
                destinationAmount,
                sourceAccount.getCurrency(),
                destinationAccount.getCurrency(),
                exchangeRate,
                transferDate,
                description
        );
    }

    private Account findAccountForUpdate(
            Long accountId,
            Integer userId
    ) {
        return accountRepository
                .findByIdAndUserIdForUpdate(
                        accountId,
                        userId
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "La cuenta "
                                        + accountId
                                        + " no existe o no pertenece al usuario"
                        )
                );
    }

    private Category findTransferCategory(
            String name,
            CategoryType type
    ) {
        return categoryRepository
                .findByNameIgnoreCaseAndType(name, type)
                .orElseThrow(() ->
                        new NotFoundException(
                                "No existe la categoría requerida: "
                                        + name
                        )
                );
    }

    private void validateDifferentAccounts(
            TransferCreateDto dto
    ) {
        if (
                dto.getSourceAccountId()
                        .equals(dto.getDestinationAccountId())
        ) {
            throw new ConfictException(
                    "La cuenta de origen y destino deben ser diferentes"
            );
        }
    }

    private void validateFunds(
            Account sourceAccount,
            BigDecimal amount
    ) {
        if (
                sourceAccount
                        .getBaseBalance()
                        .compareTo(amount) < 0
        ) {
            throw new ConfictException(
                    "Fondos insuficientes en la cuenta de origen"
            );
        }
    }

    private BigDecimal determineExchangeRate(
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal providedRate
    ) {
        boolean sameCurrency =
                sourceAccount
                        .getCurrency()
                        .equalsIgnoreCase(
                                destinationAccount.getCurrency()
                        );

        if (sameCurrency) {
            return BigDecimal.ONE;
        }

        if (
                providedRate == null
                        || providedRate.compareTo(BigDecimal.ZERO) <= 0
        ) {
            throw new ConfictException(
                    "Debe proporcionar una tasa de cambio válida"
            );
        }

        return providedRate;
    }

    private String normalizeDescription(
            String description
    ) {
        if (
                description == null
                        || description.isBlank()
        ) {
            return "Transferencia entre cuentas";
        }

        return description.trim();
    }
}