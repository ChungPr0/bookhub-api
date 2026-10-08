package com.chungpr0.bookhub.modules.order.repository.specification;

import com.chungpr0.bookhub.modules.order.entity.Voucher;
import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class VoucherSpecification {

    private VoucherSpecification() {
    }

    public static Specification<Voucher> filter(
            String keyword,
            VoucherStatus status,
            VoucherDiscountType discountType
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String search = "%" + keyword.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("code")), search);
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), search);
                predicates.add(cb.or(codeMatch, nameMatch));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (discountType != null) {
                predicates.add(cb.equal(root.get("discountType"), discountType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

