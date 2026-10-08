package com.chungpr0.bookhub.modules.catalog.repository;

import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findBySlug(String slug);

    Optional<Book> findBySlugAndStatus(String slug, BookStatus status);

    @Query("SELECT b FROM Book b WHERE b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE ORDER BY b.soldCount DESC")
    List<Book> findTopBestSellers(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE AND (b.category.slug = :categorySlug OR b.category.parent.slug = :categorySlug OR b.category.parent.parent.slug = :categorySlug) ORDER BY b.soldCount DESC")
    List<Book> findTopBestSellersByCategory(@Param("categorySlug") String categorySlug, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE ORDER BY b.createdAt DESC")
    List<Book> findNewArrivals(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE AND (b.category.slug = :categorySlug OR b.category.parent.slug = :categorySlug OR b.category.parent.parent.slug = :categorySlug) ORDER BY b.createdAt DESC")
    List<Book> findNewArrivalsByCategory(@Param("categorySlug") String categorySlug, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE AND LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchAutocompleteBooks(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE ORDER BY b.soldCount DESC, b.avgRating DESC")
    List<Book> findTopBooksByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.publisher.id = :publisherId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE ORDER BY b.soldCount DESC, b.avgRating DESC")
    List<Book> findTopBooksByPublisherId(@Param("publisherId") Long publisherId, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE a.id IN :authorIds AND b.id != :excludeBookId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE")
    List<Book> findRelatedBooksByAuthors(@Param("authorIds") List<Long> authorIds, @Param("excludeBookId") Long excludeBookId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId AND b.id != :excludeBookId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE")
    List<Book> findRelatedBooksByCategory(@Param("categoryId") Long categoryId, @Param("excludeBookId") Long excludeBookId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.publisher.id = :publisherId AND b.id != :excludeBookId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE")
    List<Book> findRelatedBooksByPublisher(@Param("publisherId") Long publisherId, @Param("excludeBookId") Long excludeBookId, Pageable pageable);
}

