package com.chungpr0.bookhub.modules.catalog.repository;

import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    Optional<Publisher> findBySlug(String slug);

    List<Publisher> findTop10ByNameContainingIgnoreCase(String keyword);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.publisher.id = :publisherId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE")
    int countActiveBooksByPublisherId(@Param("publisherId") Long publisherId);
}

