package com.server.app.repositories;

import com.server.app.entities.Account;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Page<Account> findAllByUserId(
            Integer userId,
            Pageable pageable
    );

    Optional<Account> findByIdAndUserId(
            Long id,
            Integer userId
    );

    boolean existsByAliasIgnoreCaseAndUserId(
            String alias,
            Integer userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.id = :accountId
              AND a.user.id = :userId
            """)
    Optional<Account> findByIdAndUserIdForUpdate(
            @Param("accountId") Long accountId,
            @Param("userId") Integer userId
    );
}