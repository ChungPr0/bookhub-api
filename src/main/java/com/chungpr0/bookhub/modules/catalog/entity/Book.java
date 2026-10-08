package com.chungpr0.bookhub.modules.catalog.entity;

import com.chungpr0.bookhub.common.entity.BaseEntity;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.StockStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 13, unique = true)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @OrderColumn(name = "sort_order")
    @Builder.Default
    private List<Author> authors = new ArrayList<>();

    @Column(length = 150)
    private String translator;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String language = "VI";

    @Column(name = "page_count")
    private Integer pageCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "cover_type", nullable = false, length = 20)
    @Builder.Default
    private CoverType coverType = CoverType.PAPERBACK;

    @Column(length = 50)
    private String dimensions;

    @Column(name = "weight_gram")
    private Integer weightGram;

    @Column(name = "original_price", nullable = false)
    private Long originalPrice;

    @Column(name = "sale_price", nullable = false)
    private Long salePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookStatus status = BookStatus.ACTIVE;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private int stockQuantity = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private int lowStockThreshold = 10;

    @Column(name = "sold_count", nullable = false)
    @Builder.Default
    private int soldCount = 0;

    @Column(name = "avg_rating", nullable = false)
    @Builder.Default
    private Double avgRating = 0.0;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private int reviewCount = 0;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private int version = 0;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<BookImage> images = new ArrayList<>();

    public int getDiscountPercent() {
        if (originalPrice == null || originalPrice <= 0 || salePrice == null) {
            return 0;
        }
        if (salePrice >= originalPrice) {
            return 0;
        }
        return (int) Math.round(((double) (originalPrice - salePrice) / originalPrice) * 100);
    }

    public StockStatus getStockStatus() {
        if (stockQuantity <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        if (stockQuantity <= lowStockThreshold) {
            return StockStatus.LOW_STOCK;
        }
        return StockStatus.IN_STOCK;
    }

    public int getMaxPurchasableQuantity() {
        if (stockQuantity <= 0) {
            return 0;
        }
        return Math.min(stockQuantity, 99);
    }

    public String getThumbnailUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getUrl();
        }
        return null;
    }
}

