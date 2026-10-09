package com.chungpr0.bookhub.modules.catalog.repository;

import com.chungpr0.bookhub.modules.catalog.entity.Wishlist;
import com.chungpr0.bookhub.modules.catalog.entity.WishlistId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId>, JpaSpecificationExecutor<Wishlist> {

    boolean existsByIdCustomerIdAndIdBookId(Long customerId, Long bookId);

    long countByIdCustomerId(Long customerId);

    Optional<Wishlist> findByIdCustomerIdAndIdBookId(Long customerId, Long bookId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.id.customerId = :customerId AND w.id.bookId = :bookId")
    void deleteByIdCustomerIdAndIdBookId(@Param("customerId") Long customerId, @Param("bookId") Long bookId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.id.bookId = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);

    Page<Wishlist> findByIdCustomerId(Long customerId, Pageable pageable);
}
