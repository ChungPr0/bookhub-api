package com.chungpr0.bookhub.modules.order.repository;

import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findByCode(PaymentMethodCode code);

    List<PaymentMethod> findByIsActiveTrueOrderBySortOrderAsc();

    List<PaymentMethod> findAllByOrderBySortOrderAsc();

    long countByIsActiveTrue();
}

