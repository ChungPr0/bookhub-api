package com.chungpr0.bookhub.modules.inventory.repository;

import com.chungpr0.bookhub.modules.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long>, JpaSpecificationExecutor<InventoryTransaction> {

    Optional<InventoryTransaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT it FROM InventoryTransaction it WHERE it.book.id = :bookId ORDER BY it.createdAt DESC, it.id DESC")
    List<InventoryTransaction> findByBookId(@Param("bookId") Long bookId);
}

