package com.server.app.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.entities.Movement;

public interface MovementRepository extends JpaRepository<Movement, Long> {

    Page<Movement> findAllByAccountUserId(
            Integer userId,
            Pageable pageable
    );

    Page<Movement> findAllByAccountUserIdAndDateBetween(
            Integer userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}