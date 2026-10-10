package com.chungpr0.bookhub.modules.inventory.repository;

import com.chungpr0.bookhub.modules.inventory.entity.Batch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long>, JpaSpecificationExecutor<Batch> {

    Optional<Batch> findByBatchCode(String batchCode);

    List<Batch> findByBookIdAndQuantityRemainingGreaterThanOrderByImportDateAscIdAsc(Long bookId, int remaining);

    List<Batch> findByReceiptId(Long receiptId);

    @Query("SELECT b FROM Batch b WHERE b.book.id = :bookId ORDER BY b.importDate DESC, b.id DESC")
    List<Batch> findLatestByBookId(@Param("bookId") Long bookId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.quantityRemaining * b.importPrice), 0) FROM Batch b WHERE b.quantityRemaining > 0")
    Long sumTotalStockValue();

    @Query("SELECT COALESCE(SUM(b.quantityRemaining * b.importPrice), 0) FROM Batch b WHERE b.book.id = :bookId AND b.quantityRemaining > 0")
    Long sumStockValueByBookId(@Param("bookId") Long bookId);

    @Query("SELECT AVG(b.importPrice) FROM Batch b WHERE b.book.id = :bookId AND b.quantityRemaining > 0")
    Double findAvgImportPriceByBookId(@Param("bookId") Long bookId);
}

