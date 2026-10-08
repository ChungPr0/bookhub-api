package com.chungpr0.bookhub.modules.auth.entity;

import com.chungpr0.bookhub.common.entity.BaseEntity;
import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String username; // Normalized phone: 0xxxxxxxxx

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @lombok.Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "token_version", nullable = false)
    @lombok.Builder.Default
    private int tokenVersion = 0;

    @Column(name = "failed_login_count", nullable = false)
    @lombok.Builder.Default
    private int failedLoginCount = 0;

    @Column(name = "login_locked_until")
    private OffsetDateTime loginLockedUntil;

    @Column(name = "locked_reason", length = 255)
    private String lockedReason;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;
}
