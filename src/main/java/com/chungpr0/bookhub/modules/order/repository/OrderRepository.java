package com.chungpr0.bookhub.modules.order.repository;

import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
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

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    List<Order> findByStatusAndCompletedAtBetween(OrderStatus status, OffsetDateTime from, OffsetDateTime to);

    List<Order> findByStatusAndCompletedAtBetweenOrderByCompletedAtAsc(OrderStatus status, OffsetDateTime from, OffsetDateTime to);

    long countByStatusAndCompletedAtBetween(OrderStatus status, OffsetDateTime from, OffsetDateTime to);

    long countByStatusAndCancelledAtBetween(OrderStatus status, OffsetDateTime from, OffsetDateTime to);
}

