package com.server.app.repositories;

import com.server.app.entities.Category;
import com.server.app.enums.CategoryType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Page<Category> findAllByOrderByNameAsc(
            Pageable pageable
    );

    Optional<Category> findByNameIgnoreCaseAndType(
            String name,
            CategoryType type
    );
}