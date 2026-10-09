package com.chungpr0.bookhub.modules.inventory.repository;

import com.chungpr0.bookhub.modules.inventory.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT COUNT(sr) FROM StockReceipt sr WHERE sr.supplier.id = :supplierId")
    Long countReceiptsBySupplierId(@Param("supplierId") Long supplierId);

    @Query("SELECT COALESCE(SUM(sr.totalCost), 0) FROM StockReceipt sr WHERE sr.supplier.id = :supplierId")
    Long sumTotalCostBySupplierId(@Param("supplierId") Long supplierId);
}

