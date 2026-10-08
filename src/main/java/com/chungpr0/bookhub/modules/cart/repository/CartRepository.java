package com.chungpr0.bookhub.modules.cart.repository;

import com.chungpr0.bookhub.modules.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomerId(Long customerId);
}

