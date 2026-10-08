package com.chungpr0.bookhub.modules.order.repository;

import com.chungpr0.bookhub.modules.order.entity.ShippingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingConfigRepository extends JpaRepository<ShippingConfig, Long> {

    Optional<ShippingConfig> findFirstByOrderByIdAsc();
}

