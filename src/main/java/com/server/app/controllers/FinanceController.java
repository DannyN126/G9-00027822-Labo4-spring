package com.server.app.controllers;

import com.server.app.dto.finance.request.AccountCreateDto;
import com.server.app.dto.finance.request.TransferCreateDto;
import com.server.app.dto.finance.response.AccountResponse;
import com.server.app.dto.finance.response.CategoryResponse;
import com.server.app.dto.finance.response.MovementResponse;
import com.server.app.dto.finance.response.TransferResponse;
import com.server.app.dto.response.Pagination;
import com.server.app.dto.response.PaginationMeta;
import com.server.app.entities.User;
import com.server.app.services.AccountService;
import com.server.app.services.CategoryService;
import com.server.app.services.MovementService;
import com.server.app.services.TransferService;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/finanzas")
@AllArgsConstructor
public class FinanceController {

    private final AccountService accountService;
    private final CategoryService categoryService;
    private final MovementService movementService;
    private final TransferService transferService;

    /**
     * Lista las cuentas pertenecientes al usuario autenticado.
     */
    @GetMapping("/cuentas")
    public ResponseEntity<Pagination<AccountResponse>> findAccounts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AccountResponse> result =
                accountService.findAllByUser(user, page, size);

        return ResponseEntity.ok(toPagination(result));
    }

    /**
     * Crea una cuenta para el usuario autenticado.
     */
    @PostMapping("/cuentas")
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AccountCreateDto dto
    ) {
        AccountResponse account =
                accountService.create(user, dto);

        return ResponseEntity.ok(account);
    }

    /**
     * Lista los movimientos del usuario con filtros opcionales por fecha.
     */
    @GetMapping("/movimientos")
    public ResponseEntity<Pagination<MovementResponse>> findMovements(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin
    ) {
        Page<MovementResponse> result =
                movementService.findAllByUser(
                        user,
                        page,
                        size,
                        fechaInicio,
                        fechaFin
                );

        return ResponseEntity.ok(toPagination(result));
    }

    /**
     * Realiza una transferencia entre dos cuentas del usuario.
     */
    @PostMapping("/transferencias")
    public ResponseEntity<TransferResponse> transfer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferCreateDto dto
    ) {
        TransferResponse response =
                transferService.transfer(user, dto);

        return ResponseEntity.ok(response);
    }

    /**
     * Lista las categorías disponibles.
     */
    @GetMapping("/categorias")
    public ResponseEntity<Pagination<CategoryResponse>> findCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CategoryResponse> result =
                categoryService.findAll(page, size);

        return ResponseEntity.ok(toPagination(result));
    }

    private <T> Pagination<T> toPagination(Page<T> page) {
        return new Pagination<>(
                page.getContent(),
                new PaginationMeta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalPages(),
                        page.getTotalElements()
                )
        );
    }
}