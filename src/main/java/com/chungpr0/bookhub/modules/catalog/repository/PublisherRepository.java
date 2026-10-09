package com.chungpr0.bookhub.modules.catalog.repository;

import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Long>, JpaSpecificationExecutor<Publisher> {

    Optional<Publisher> findBySlug(String slug);

    List<Publisher> findTop10ByNameContainingIgnoreCase(String keyword);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.publisher.id = :publisherId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE")
    int countActiveBooksByPublisherId(@Param("publisherId") Long publisherId);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.publisher.id = :publisherId")
    long countTotalBooksByPublisherId(@Param("publisherId") Long publisherId);
}

