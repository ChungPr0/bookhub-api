package com.chungpr0.bookhub.modules.cart.repository.specification;

import com.chungpr0.bookhub.modules.cart.dto.request.CartSearchFilter;
import com.chungpr0.bookhub.modules.cart.entity.Cart;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CartSpecification {

    private CartSpecification() {
    }

    public static Specification<Cart> filter(CartSearchFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate fullNamePredicate = cb.like(cb.lower(root.get("customer").get("fullName")), pattern);
                Predicate phonePredicate = cb.like(root.get("customer").get("phone"), pattern);
                predicates.add(cb.or(fullNamePredicate, phonePredicate));
            }

            if (filter.getHasItems() != null) {
                if (filter.getHasItems()) {
                    predicates.add(cb.isNotEmpty(root.get("items")));
                } else {
                    predicates.add(cb.isEmpty(root.get("items")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

