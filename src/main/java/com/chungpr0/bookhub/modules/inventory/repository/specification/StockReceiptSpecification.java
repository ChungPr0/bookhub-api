package com.chungpr0.bookhub.modules.inventory.repository.specification;

import com.chungpr0.bookhub.modules.inventory.entity.StockReceipt;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class StockReceiptSpecification {

    private StockReceiptSpecification() {
    }

    public static Specification<StockReceipt> filter(
            String keyword,
            Long supplierId,
            Long createdBy,
            LocalDate importDateFrom,
            LocalDate importDateTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String search = "%" + keyword.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("receiptCode")), search);
                Predicate noteMatch = cb.like(cb.lower(root.get("note")), search);
                predicates.add(cb.or(codeMatch, noteMatch));
            }

            if (supplierId != null) {
                predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));
            }

            if (createdBy != null) {
                predicates.add(cb.equal(root.get("createdBy"), createdBy));
            }

            if (importDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("importDate"), importDateFrom));
            }

            if (importDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("importDate"), importDateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

