package com.chungpr0.bookhub.modules.inventory.repository.specification;

import com.chungpr0.bookhub.modules.inventory.entity.Batch;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class BatchSpecification {

    private BatchSpecification() {
    }

    public static Specification<Batch> filter(
            Long bookId,
            Long receiptId,
            Long supplierId,
            Boolean hasRemaining
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookId != null) {
                predicates.add(cb.equal(root.get("book").get("id"), bookId));
            }

            if (receiptId != null) {
                predicates.add(cb.equal(root.get("receipt").get("id"), receiptId));
            }

            if (supplierId != null) {
                predicates.add(cb.equal(root.get("receipt").get("supplier").get("id"), supplierId));
            }

            if (Boolean.TRUE.equals(hasRemaining)) {
                predicates.add(cb.greaterThan(root.get("quantityRemaining"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

