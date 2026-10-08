package com.chungpr0.bookhub.modules.order.repository;

import com.chungpr0.bookhub.modules.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionRef(String transactionRef);

    List<Payment> findByOrderId(Long orderId);
}

