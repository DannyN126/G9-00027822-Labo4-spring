package com.server.app.dto.finance.response;

import com.server.app.enums.CategoryType;

public record CategoryResponse(
        Long id,
        String name,
        CategoryType type,
        Long parentCategoryId,
        String parentCategoryName
) {
}