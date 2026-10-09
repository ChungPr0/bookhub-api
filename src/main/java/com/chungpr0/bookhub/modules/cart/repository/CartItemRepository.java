package com.chungpr0.bookhub.modules.cart.repository;

import com.chungpr0.bookhub.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndBookId(Long cartId, Long bookId);

    int countByCartId(Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.book.id = :bookId")
    void deleteByCartIdAndBookId(@Param("cartId") Long cartId, @Param("bookId") Long bookId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.book.id IN :bookIds")
    void deleteByCartIdAndBookIdIn(@Param("cartId") Long cartId, @Param("bookIds") Collection<Long> bookIds);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}

