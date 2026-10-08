package com.chungpr0.bookhub.modules.user.repository.specification;

import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import org.springframework.data.jpa.domain.Specification;

public class PointTransactionSpecification {

    public static Specification<PointTransaction> hasCustomerId(Long customerId) {
        return (root, query, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<PointTransaction> hasType(PointTransactionType type) {
        return (root, query, cb) -> {
            if (type == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("type"), type);
        };
    }

    public static Specification<PointTransaction> filter(Long customerId, PointTransactionType type) {
        return Specification.where(hasCustomerId(customerId)).and(hasType(type));
    }
}

