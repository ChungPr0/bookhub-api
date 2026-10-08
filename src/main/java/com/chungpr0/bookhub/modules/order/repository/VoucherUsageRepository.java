package com.chungpr0.bookhub.modules.order.repository;

import com.chungpr0.bookhub.modules.order.entity.VoucherUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    long countByVoucherIdAndCustomerIdAndReleasedAtIsNull(Long voucherId, Long customerId);

    Optional<VoucherUsage> findByOrderId(Long orderId);

    Page<VoucherUsage> findByVoucherId(Long voucherId, Pageable pageable);
}

