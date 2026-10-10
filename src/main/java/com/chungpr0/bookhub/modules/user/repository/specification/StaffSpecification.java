package com.chungpr0.bookhub.modules.user.repository.specification;

import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.user.dto.request.StaffFilterRequest;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class StaffSpecification {

    private StaffSpecification() {
        // Prevent instantiation
    }

    public static Specification<Staff> filter(StaffFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            Join<Staff, Account> accountJoin = root.join("account", JoinType.LEFT);

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String keywordPattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate fullNameMatch = cb.like(cb.lower(root.get("fullName")), keywordPattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), keywordPattern);
                Predicate phoneMatch = cb.like(cb.lower(accountJoin.get("username")), keywordPattern);
                predicates.add(cb.or(fullNameMatch, emailMatch, phoneMatch));
            }

            if (filter.getRole() != null) {
                predicates.add(cb.equal(accountJoin.get("role"), filter.getRole()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(accountJoin.get("status"), filter.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

