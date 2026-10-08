package com.chungpr0.bookhub.modules.cart.repository.specification;

import com.chungpr0.bookhub.modules.cart.dto.request.WishlistFilter;
import com.chungpr0.bookhub.modules.catalog.entity.Wishlist;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WishlistSpecification {

    private WishlistSpecification() {
    }

    public static Specification<Wishlist> filter(Long customerId, WishlistFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("id").get("customerId"), customerId));

            if (filter != null) {
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("book").get("title")), pattern));
                }

                if (filter.getCategoryId() != null) {
                    predicates.add(cb.equal(root.get("book").get("category").get("id"), filter.getCategoryId()));
                }

                if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                    predicates.add(cb.greaterThan(root.get("book").get("stockQuantity"), 0));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

