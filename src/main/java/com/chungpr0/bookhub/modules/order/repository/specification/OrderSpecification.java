package com.chungpr0.bookhub.modules.order.repository.specification;

import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class OrderSpecification {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private OrderSpecification() {
    }

    public static Specification<Order> filter(
            Long customerId,
            String keyword,
            Collection<OrderStatus> statuses,
            PaymentStatus paymentStatus,
            PaymentMethodCode paymentMethod,
            LocalDate createdFrom,
            LocalDate createdTo,
            Long finalAmountFrom,
            Long finalAmountTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }

            if (StringUtils.hasText(keyword)) {
                String search = "%" + keyword.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("orderCode")), search);
                Predicate nameMatch = cb.like(cb.lower(root.get("receiverName")), search);
                Predicate phoneMatch = cb.like(root.get("receiverPhone"), search);
                predicates.add(cb.or(codeMatch, nameMatch, phoneMatch));
            }

            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            if (paymentStatus != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            }

            if (paymentMethod != null) {
                predicates.add(cb.equal(root.get("paymentMethod").get("code"), paymentMethod));
            }

            if (createdFrom != null) {
                OffsetDateTime fromDateTime = createdFrom.atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }

            if (createdTo != null) {
                OffsetDateTime toDateTime = createdTo.plusDays(1).atStartOfDay(VIETNAM_ZONE).minusNanos(1).toOffsetDateTime();
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
            }

            if (finalAmountFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("finalAmount"), finalAmountFrom));
            }

            if (finalAmountTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("finalAmount"), finalAmountTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

