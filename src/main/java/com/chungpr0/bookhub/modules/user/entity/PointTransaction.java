package com.chungpr0.bookhub.modules.user.entity;

import com.chungpr0.bookhub.common.entity.BaseEntity;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "point_transactions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    @Column(nullable = false)
    private int points;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "note")
    private String note;

    @Column(name = "created_by")
    private Long createdBy;
}

