package com.chungpr0.bookhub.modules.order.entity;

import com.chungpr0.bookhub.common.entity.BaseEntity;
import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherState;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private VoucherDiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private Long discountValue;

    @Column(name = "max_discount_amount")
    private Long maxDiscountAmount;

    @Column(name = "min_order_amount", nullable = false)
    @Builder.Default
    private Long minOrderAmount = 0L;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_limit_per_customer", nullable = false)
    @Builder.Default
    private Integer usageLimitPerCustomer = 1;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "expiration_date", nullable = false)
    private OffsetDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ACTIVE;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private int version = 0;

    public VoucherState computeState(OffsetDateTime now) {
        if (status == VoucherStatus.INACTIVE) {
            return VoucherState.DISABLED;
        }
        if (now.isBefore(startDate)) {
            return VoucherState.UPCOMING;
        }
        if (now.isAfter(expirationDate)) {
            return VoucherState.EXPIRED;
        }
        if (usageLimit != null && usedCount >= usageLimit) {
            return VoucherState.EXHAUSTED;
        }
        return VoucherState.ACTIVE;
    }
}

