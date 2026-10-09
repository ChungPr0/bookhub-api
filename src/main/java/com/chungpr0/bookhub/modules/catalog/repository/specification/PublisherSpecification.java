package com.chungpr0.bookhub.modules.catalog.repository.specification;

import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import org.springframework.data.jpa.domain.Specification;

public final class PublisherSpecification {

    private PublisherSpecification() {
        // Suppress default constructor for utility/specification class
    }

    public static Specification<Publisher> withKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
        };
    }
}

