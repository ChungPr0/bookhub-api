package com.chungpr0.bookhub.modules.order.entity;

import com.chungpr0.bookhub.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "shipping_configs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "standard_base_fee", nullable = false)
    @Builder.Default
    private Long standardBaseFee = 30000L;

    @Column(name = "express_base_fee", nullable = false)
    @Builder.Default
    private Long expressBaseFee = 45000L;

    @Column(name = "same_day_base_fee", nullable = false)
    @Builder.Default
    private Long sameDayBaseFee = 60000L;

    @Column(name = "free_shipping_threshold", nullable = false)
    @Builder.Default
    private Long freeShippingThreshold = 300000L;

    @Column(name = "max_free_shipping_subsidy", nullable = false)
    @Builder.Default
    private Long maxFreeShippingSubsidy = 30000L;

    @Column(name = "standard_max_weight_gram", nullable = false)
    @Builder.Default
    private Integer standardMaxWeightGram = 2000;

    @Column(name = "overweight_unit_gram", nullable = false)
    @Builder.Default
    private Integer overweightUnitGram = 500;

    @Column(name = "overweight_surcharge", nullable = false)
    @Builder.Default
    private Long overweightSurcharge = 5000L;

    @Column(name = "updated_by_account_id")
    private Long updatedByAccountId;
}

