package com.chungpr0.bookhub.modules.auth.repository;

import com.chungpr0.bookhub.modules.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByFamilyId(String familyId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :revokedAt WHERE r.familyId = :familyId AND r.revokedAt IS NULL")
    int revokeAllByFamilyId(@Param("familyId") String familyId, @Param("revokedAt") OffsetDateTime revokedAt);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :revokedAt WHERE r.account.id = :accountId AND r.revokedAt IS NULL")
    int revokeAllByAccountId(@Param("accountId") Long accountId, @Param("revokedAt") OffsetDateTime revokedAt);
}

