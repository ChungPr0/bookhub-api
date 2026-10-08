package com.chungpr0.bookhub.modules.catalog.repository.specification;

import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.modules.catalog.dto.request.BookSearchFilter;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> buildSpecification(BookSearchFilter filter, BookStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (filter != null) {
                if (StringUtils.hasText(filter.getKeyword())) {
                    String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    Predicate titlePredicate = cb.like(cb.lower(root.get("title")), pattern);
                    Predicate isbnPredicate = cb.like(cb.lower(root.get("isbn")), pattern);

                    Join<Book, Author> authorJoin = root.join("authors", JoinType.LEFT);
                    Predicate authorPredicate = cb.like(cb.lower(authorJoin.get("name")), pattern);

                    predicates.add(cb.or(titlePredicate, isbnPredicate, authorPredicate));
                    query.distinct(true);
                }

                if (StringUtils.hasText(filter.getCategorySlug())) {
                    Path<Category> cat = root.get("category");
                    Path<Category> parentCat = cat.get("parent");
                    Path<Category> grandParentCat = parentCat.get("parent");

                    Predicate direct = cb.equal(cat.get("slug"), filter.getCategorySlug());
                    Predicate level2 = cb.equal(parentCat.get("slug"), filter.getCategorySlug());
                    Predicate level3 = cb.equal(grandParentCat.get("slug"), filter.getCategorySlug());

                    predicates.add(cb.or(direct, level2, level3));
                }

                if (StringUtils.hasText(filter.getAuthorSlug())) {
                    Join<Book, Author> authorJoin = root.join("authors", JoinType.LEFT);
                    predicates.add(cb.equal(authorJoin.get("slug"), filter.getAuthorSlug()));
                    query.distinct(true);
                }

                if (StringUtils.hasText(filter.getPublisherSlug())) {
                    predicates.add(cb.equal(root.get("publisher").get("slug"), filter.getPublisherSlug()));
                }

                if (StringUtils.hasText(filter.getLanguage())) {
                    predicates.add(cb.equal(cb.upper(root.get("language")), filter.getLanguage().trim().toUpperCase()));
                }

                if (StringUtils.hasText(filter.getCoverType())) {
                    try {
                        CoverType ct = CoverType.valueOf(filter.getCoverType().trim().toUpperCase());
                        predicates.add(cb.equal(root.get("coverType"), ct));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                if (filter.getPriceFrom() != null && filter.getPriceFrom() >= 0) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("salePrice"), filter.getPriceFrom()));
                }

                if (filter.getPriceTo() != null && filter.getPriceTo() >= 0) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("salePrice"), filter.getPriceTo()));
                }

                if (filter.getMinRating() != null && filter.getMinRating() > 0) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("avgRating"), filter.getMinRating().doubleValue()));
                }

                if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                    predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

