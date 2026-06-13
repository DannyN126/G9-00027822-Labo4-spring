package com.server.app.services;

import com.server.app.dto.finance.response.CategoryResponse;
import com.server.app.mappers.CategoryMapper;
import com.server.app.repositories.CategoryRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(
            int page,
            int size
    ) {
        validatePagination(page, size);

        return categoryRepository
                .findAllByOrderByNameAsc(
                        PageRequest.of(page, size)
                )
                .map(categoryMapper::toResponse);
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