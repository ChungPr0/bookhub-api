package com.chungpr0.bookhub.modules.order.repository;

import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByOrderCodeAndCustomerId(String orderCode, Long customerId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    long countByCustomerId(Long customerId);

    long countByCustomerIdAndStatus(Long customerId, OrderStatus status);

    long countByStatus(OrderStatus status);

    long countByPaymentMethodCodeAndPaymentStatus(PaymentMethodCode code, PaymentStatus paymentStatus);

    long countByPaymentStatus(PaymentStatus paymentStatus);
}

