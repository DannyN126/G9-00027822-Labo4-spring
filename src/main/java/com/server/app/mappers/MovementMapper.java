package com.server.app.mappers;

import org.springframework.stereotype.Component;

import com.server.app.dto.finance.response.MovementResponse;
import com.server.app.entities.Movement;

@Component
public class MovementMapper {

    public MovementResponse toResponse(Movement movement) {
        return new MovementResponse(
                movement.getId(),
                movement.getAmount(),
                movement.getOriginalCurrency(),
                movement.getExchangeRate(),
                movement.getDate(),
                movement.getDescription(),
                movement.getAccount().getId(),
                movement.getAccount().getAlias(),
                movement.getCategory().getId(),
                movement.getCategory().getName(),
                movement.getCategory().getType()
        );
    }
}