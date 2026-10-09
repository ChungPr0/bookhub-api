package com.chungpr0.bookhub.modules.catalog.repository.specification;

import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.modules.catalog.entity.Review;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AdminReviewSpecification {

    private AdminReviewSpecification() {
        // Suppress default constructor for utility/specification class
    }

    public static Specification<Review> filter(
            String keyword,
            Long bookId,
            Integer rating,
            ReviewStatus status,
            Boolean hasReply
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate contentMatch = cb.like(cb.lower(root.get("content")), pattern);
                Predicate bookTitleMatch = cb.like(cb.lower(root.get("book").get("title")), pattern);
                Predicate customerNameMatch = cb.like(cb.lower(root.get("customer").get("fullName")), pattern);
                Predicate customerPhoneMatch = cb.like(cb.lower(root.get("customer").get("phone")), pattern);
                predicates.add(cb.or(contentMatch, bookTitleMatch, customerNameMatch, customerPhoneMatch));
            }

            if (bookId != null) {
                predicates.add(cb.equal(root.get("book").get("id"), bookId));
            }

            if (rating != null) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (hasReply != null) {
                if (hasReply) {
                    predicates.add(cb.isNotNull(root.get("adminReply")));
                } else {
                    predicates.add(cb.isNull(root.get("adminReply")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

