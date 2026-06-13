package com.server.app.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            unique = true,
            nullable = false
    )
    private String username;

    @Column
    private String name;

    @Column
    private String surname;

    @Column(
            unique = true,
            nullable = false
    )
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean blocked = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @PrePersist
    @PreUpdate
    private void encryptPassword() {
        if (
                password != null
                        && !password.startsWith("$2a$")
                        && !password.startsWith("$2b$")
                        && !password.startsWith("$2y$")
        ) {
            this.password =
                    new BCryptPasswordEncoder().encode(password);
        }
    }
}