package com.server.app.services;

import com.server.app.dto.finance.response.MovementResponse;
import com.server.app.entities.User;
import com.server.app.exceptions.ConfictException;
import com.server.app.mappers.MovementMapper;
import com.server.app.repositories.MovementRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class MovementService {

    private final MovementRepository movementRepository;
    private final MovementMapper movementMapper;

    @Transactional(readOnly = true)
    public Page<MovementResponse> findAllByUser(
            User user,
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validatePagination(page, size);
        validateDates(startDate, endDate);

        PageRequest pageable = PageRequest.of(page, size);

        if (startDate == null && endDate == null) {
            return movementRepository
                    .findAllByAccountUserId(
                            user.getId(),
                            pageable
                    )
                    .map(movementMapper::toResponse);
        }

        LocalDate effectiveStart =
                startDate != null
                        ? startDate
                        : LocalDate.of(1970, 1, 1);

        LocalDate effectiveEnd =
                endDate != null
                        ? endDate
                        : LocalDate.of(9999, 12, 31);

        LocalDateTime startDateTime =
                effectiveStart.atStartOfDay();

        LocalDateTime endDateTime =
                effectiveEnd.atTime(
                        23,
                        59,
                        59,
                        999_999_999
                );

        return movementRepository
                .findAllByAccountUserIdAndDateBetween(
                        user.getId(),
                        startDateTime,
                        endDateTime,
                        pageable
                )
                .map(movementMapper::toResponse);
    }

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (
                startDate != null
                        && endDate != null
                        && startDate.isAfter(endDate)
        ) {
            throw new ConfictException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
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