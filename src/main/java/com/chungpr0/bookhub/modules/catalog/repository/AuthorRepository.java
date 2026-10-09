package com.chungpr0.bookhub.modules.catalog.repository;

import com.chungpr0.bookhub.modules.catalog.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {

    Optional<Author> findBySlug(String slug);

    List<Author> findTop10ByNameContainingIgnoreCase(String keyword);

    @Query("SELECT COUNT(b) FROM Book b JOIN b.authors a WHERE a.id = :authorId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE")
    int countActiveBooksByAuthorId(@Param("authorId") Long authorId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("SELECT COUNT(b) FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    long countTotalBooksByAuthorId(@Param("authorId") Long authorId);
}

