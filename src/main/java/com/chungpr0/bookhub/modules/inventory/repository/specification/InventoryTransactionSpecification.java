package com.chungpr0.bookhub.modules.inventory.repository.specification;

import com.chungpr0.bookhub.modules.inventory.entity.InventoryTransaction;
import com.chungpr0.bookhub.modules.inventory.enums.InventoryTransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class InventoryTransactionSpecification {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private InventoryTransactionSpecification() {
    }

    public static Specification<InventoryTransaction> filter(
            Long bookId,
            InventoryTransactionType type,
            LocalDate createdFrom,
            LocalDate createdTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookId != null) {
                predicates.add(cb.equal(root.get("book").get("id"), bookId));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (createdFrom != null) {
                OffsetDateTime fromDateTime = createdFrom.atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }

            if (createdTo != null) {
                OffsetDateTime toDateTime = createdTo.plusDays(1).atStartOfDay(VIETNAM_ZONE).minusNanos(1).toOffsetDateTime();
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

