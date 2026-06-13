package com.server.app.mappers;

import org.springframework.stereotype.Component;

import com.server.app.dto.finance.response.CategoryResponse;
import com.server.app.entities.Category;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        Long parentId = category.getParentCategory() != null
                ? category.getParentCategory().getId()
                : null;

        String parentName = category.getParentCategory() != null
                ? category.getParentCategory().getName()
                : null;

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                parentId,
                parentName
        );
    }
}