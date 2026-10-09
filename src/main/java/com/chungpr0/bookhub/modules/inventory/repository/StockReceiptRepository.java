package com.chungpr0.bookhub.modules.inventory.repository;

import com.chungpr0.bookhub.modules.inventory.entity.StockReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StockReceiptRepository extends JpaRepository<StockReceipt, Long>, JpaSpecificationExecutor<StockReceipt> {

    Optional<StockReceipt> findByReceiptCode(String receiptCode);

    Optional<StockReceipt> findByIdempotencyKey(String idempotencyKey);

    long countBySupplierId(Long supplierId);
}

