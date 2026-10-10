package com.chungpr0.bookhub.modules.user.repository.specification;

import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.user.dto.request.CustomerFilterRequest;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class CustomerSpecification {

    private static final ZoneOffset VIETNAM_OFFSET = ZoneOffset.ofHours(7);

    private CustomerSpecification() {
        // Prevent instantiation
    }

    public static Specification<Customer> filter(CustomerFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            Join<Customer, Account> accountJoin = root.join("account", JoinType.LEFT);

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String keywordPattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate fullNameMatch = cb.like(cb.lower(root.get("fullName")), keywordPattern);
                Predicate phoneMatch = cb.like(cb.lower(root.get("phone")), keywordPattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), keywordPattern);
                predicates.add(cb.or(fullNameMatch, phoneMatch, emailMatch));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(accountJoin.get("status"), filter.getStatus()));
            }

            if (filter.getTier() != null && !filter.getTier().isEmpty()) {
                predicates.add(root.get("customerTier").in(filter.getTier()));
            }

            if (filter.getCreatedFrom() != null) {
                OffsetDateTime fromDateTime = filter.getCreatedFrom().atStartOfDay().atOffset(VIETNAM_OFFSET);
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }

            if (filter.getCreatedTo() != null) {
                OffsetDateTime toDateTime = filter.getCreatedTo().atTime(LocalTime.MAX).atOffset(VIETNAM_OFFSET);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
            }

            if (filter.getTotalSpentFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalSpent"), filter.getTotalSpentFrom()));
            }

            if (filter.getTotalSpentTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalSpent"), filter.getTotalSpentTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

