package com.server.app.entities;

import com.server.app.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_alias_user",
                        columnNames = {"alias", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String alias;

    /**
     * Código ISO de la moneda.
     * Ejemplos: USD, EUR, GBP.
     */
    @Column(nullable = false, length = 3)
    private String currency;

    @Column(
            name = "base_balance",
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal baseBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (alias != null) {
            alias = alias.trim();
        }

        if (currency != null) {
            currency = currency.trim().toUpperCase();
        }

        if (baseBalance == null) {
            baseBalance = BigDecimal.ZERO;
        }
    }
}