package com.chungpr0.bookhub.modules.report.repository.specification;

import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.report.dto.request.InventoryReportFilterRequest;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class InventoryReportBookSpecification {

    private InventoryReportBookSpecification() {
        // Prevent instantiation
    }

    public static Specification<Book> filter(InventoryReportFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.getCategoryId() != null) {
                Path<Category> cat = root.get("category");
                Path<Category> parentCat = cat.get("parent");
                Path<Category> grandParentCat = parentCat.get("parent");

                Predicate direct = cb.equal(cat.get("id"), filter.getCategoryId());
                Predicate level2 = cb.equal(parentCat.get("id"), filter.getCategoryId());
                Predicate level3 = cb.equal(grandParentCat.get("id"), filter.getCategoryId());
                predicates.add(cb.or(direct, level2, level3));
            }

            if (filter.getPublisherId() != null) {
                predicates.add(cb.equal(root.get("publisher").get("id"), filter.getPublisherId()));
            }

            if (filter.getStockStatus() != null && !filter.getStockStatus().trim().isEmpty()) {
                String status = filter.getStockStatus().trim().toUpperCase();
                switch (status) {
                    case "IN_STOCK" -> predicates.add(cb.greaterThan(root.get("stockQuantity"), root.get("lowStockThreshold")));
                    case "LOW_STOCK" -> {
                        predicates.add(cb.lessThanOrEqualTo(root.get("stockQuantity"), root.get("lowStockThreshold")));
                        predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
                    }
                    case "OUT_OF_STOCK" -> predicates.add(cb.lessThanOrEqualTo(root.get("stockQuantity"), 0));
                    default -> { }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

