package com.chungpr0.bookhub.modules.catalog.repository;

import com.chungpr0.bookhub.modules.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNullOrderBySortOrderAscNameAsc();

    List<Category> findByParentIdOrderBySortOrderAscNameAsc(Long parentId);

    List<Category> findTop10ByNameContainingIgnoreCase(String keyword);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.id = :categoryId AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE")
    int countActiveBooksByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(b) FROM Book b WHERE (b.category.id = :categoryId OR b.category.parent.id = :categoryId OR b.category.parent.parent.id = :categoryId) AND b.status = com.chungpr0.bookhub.common.enums.BookStatus.ACTIVE")
    int countActiveBooksByCategoryIdRecursive(@Param("categoryId") Long categoryId);
}

