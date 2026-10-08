package com.chungpr0.bookhub.modules.auth.repository;

import com.chungpr0.bookhub.common.enums.OtpPurpose;
import com.chungpr0.bookhub.modules.auth.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByPhoneAndPurposeOrderByCreatedAtDesc(String phone, OtpPurpose purpose);

    long countByPhoneAndPurposeAndCreatedAtAfter(String phone, OtpPurpose purpose, OffsetDateTime after);

    List<Otp> findByPhoneAndPurposeAndIsUsedFalse(String phone, OtpPurpose purpose);

    @Modifying
    @Query("UPDATE Otp o SET o.isUsed = true WHERE o.phone = :phone AND o.purpose = :purpose AND o.isUsed = false")
    int invalidatePreviousOtps(@Param("phone") String phone, @Param("purpose") OtpPurpose purpose);
}

