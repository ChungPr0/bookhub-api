package com.chungpr0.bookhub.modules.inventory.repository.specification;

import com.chungpr0.bookhub.modules.inventory.entity.Supplier;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class SupplierSpecification {

    private SupplierSpecification() {
    }

    public static Specification<Supplier> filter(String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String search = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), search);
                Predicate contactMatch = cb.like(cb.lower(root.get("contactName")), search);
                Predicate phoneMatch = cb.like(root.get("phone"), search);
                Predicate taxCodeMatch = cb.like(root.get("taxCode"), search);
                predicates.add(cb.or(nameMatch, contactMatch, phoneMatch, taxCodeMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

