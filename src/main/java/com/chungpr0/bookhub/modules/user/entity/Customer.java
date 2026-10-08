package com.chungpr0.bookhub.modules.user.entity;

import com.chungpr0.bookhub.common.entity.BaseEntity;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "reward_points", nullable = false)
    @lombok.Builder.Default
    private int rewardPoints = 0;

    @Column(name = "total_spent", nullable = false)
    @lombok.Builder.Default
    private Long totalSpent = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier", nullable = false, length = 10)
    @lombok.Builder.Default
    private CustomerTier customerTier = CustomerTier.BRONZE;
}
