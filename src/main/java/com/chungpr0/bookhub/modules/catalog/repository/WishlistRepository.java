package com.chungpr0.bookhub.modules.catalog.repository;

import com.chungpr0.bookhub.modules.catalog.entity.Wishlist;
import com.chungpr0.bookhub.modules.catalog.entity.WishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

    boolean existsByIdCustomerIdAndIdBookId(Long customerId, Long bookId);
}

