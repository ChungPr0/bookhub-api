package com.chungpr0.bookhub.modules.cart.repository;

import com.chungpr0.bookhub.modules.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long>, JpaSpecificationExecutor<Cart> {

    Optional<Cart> findByCustomerId(Long customerId);

    Optional<Cart> findByCustomerAccountId(Long accountId);
}
