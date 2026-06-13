package com.server.app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "movements",
        indexes = {
                @Index(
                        name = "idx_movement_date",
                        columnList = "movement_date"
                ),
                @Index(
                        name = "idx_movement_account",
                        columnList = "account_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    @Column(
            name = "original_currency",
            nullable = false,
            length = 3
    )
    private String originalCurrency;

    @Column(
            name = "exchange_rate",
            nullable = false,
            precision = 19,
            scale = 8
    )
    private BigDecimal exchangeRate;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime date;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @PrePersist
    private void prepareBeforeInsert() {
        if (date == null) {
            date = LocalDateTime.now();
        }

        if (originalCurrency != null) {
            originalCurrency =
                    originalCurrency.trim().toUpperCase();
        }

        if (description != null) {
            description = description.trim();
        }
    }
}