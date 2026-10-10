package com.chungpr0.bookhub.modules.catalog.repository;

import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.modules.catalog.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    Page<Review> findByBookIdAndStatus(Long bookId, ReviewStatus status, Pageable pageable);

    Page<Review> findByBookIdAndStatusAndRating(Long bookId, ReviewStatus status, int rating, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId AND r.status = :status AND r.content IS NOT NULL AND TRIM(r.content) != ''")
    Page<Review> findReviewsWithContent(@Param("bookId") Long bookId, @Param("status") ReviewStatus status, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId AND r.status = :status AND r.rating = :rating AND r.content IS NOT NULL AND TRIM(r.content) != ''")
    Page<Review> findReviewsWithContentAndRating(@Param("bookId") Long bookId, @Param("status") ReviewStatus status, @Param("rating") int rating, Pageable pageable);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.status = com.chungpr0.bookhub.common.enums.ReviewStatus.VISIBLE GROUP BY r.rating")
    List<Object[]> countRatingsByStar(@Param("bookId") Long bookId);

    Optional<Review> findByOrderIdAndBookId(Long orderId, Long bookId);

    boolean existsByOrderIdAndBookId(Long orderId, Long bookId);

    Page<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Optional<Review> findByIdAndCustomerId(Long id, Long customerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.status = com.chungpr0.bookhub.common.enums.ReviewStatus.VISIBLE")
    long countVisibleReviewsByBookId(@Param("bookId") Long bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId AND r.status = com.chungpr0.bookhub.common.enums.ReviewStatus.VISIBLE")
    Double getAverageVisibleRatingByBookId(@Param("bookId") Long bookId);

    long countByAdminReplyIsNull();
}

