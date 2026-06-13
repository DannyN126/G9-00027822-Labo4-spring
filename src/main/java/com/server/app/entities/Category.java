package com.server.app.entities;

import com.server.app.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_name_type",
                        columnNames = {"name", "type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    /**
     * Autorreferencia:
     * una categoría puede pertenecer a otra categoría.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (name != null) {
            name = name.trim();
        }
    }
}