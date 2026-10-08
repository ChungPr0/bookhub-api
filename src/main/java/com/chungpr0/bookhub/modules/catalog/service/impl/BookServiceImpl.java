package com.chungpr0.bookhub.modules.catalog.service.impl;

import com.chungpr0.bookhub.common.dto.PageMeta;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.MaskingUtils;
import com.chungpr0.bookhub.modules.catalog.dto.request.BookSearchFilter;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorSummaryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookFacetsResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookImageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookReviewResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSearchResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSuggestItem;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSuggestResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryBreadcrumbItem;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategorySummaryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.FacetItemResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PriceRangeFacetResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherSummaryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.RatingSummaryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.ReviewAdminReplyResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.ReviewCustomerResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Review;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.catalog.repository.specification.BookSpecification;
import com.chungpr0.bookhub.modules.catalog.service.BookService;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Set<String> ALLOWED_SORTS = Set.of(
            "relevance",
            "createdat,desc",
            "saleprice,asc",
            "saleprice,desc",
            "soldcount,desc",
            "avgrating,desc"
    );

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public BookSearchResponse searchBooks(BookSearchFilter filter, int page, int size, String sort) {
        if (size < 1 || size > 60) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Kích thước trang phải từ 1 đến 60");
        }
        if (filter.getPriceFrom() != null && filter.getPriceTo() != null && filter.getPriceFrom() > filter.getPriceTo()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Khoảng giá không hợp lệ: giá tối thiểu không được lớn hơn giá tối đa");
        }
        if (StringUtils.hasText(filter.getCategorySlug()) && categoryRepository.findBySlug(filter.getCategorySlug().trim()).isEmpty()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (StringUtils.hasText(filter.getAuthorSlug()) && authorRepository.findBySlug(filter.getAuthorSlug().trim()).isEmpty()) {
            throw new AppException(ErrorCode.AUTHOR_NOT_FOUND);
        }
        if (StringUtils.hasText(filter.getPublisherSlug()) && publisherRepository.findBySlug(filter.getPublisherSlug().trim()).isEmpty()) {
            throw new AppException(ErrorCode.PUBLISHER_NOT_FOUND);
        }

        String sortKey = sort != null ? sort.trim().toLowerCase() : "createdat,desc";
        if (StringUtils.hasText(filter.getKeyword()) && (sort == null || sort.isBlank())) {
            sortKey = "relevance";
        }
        if (!ALLOWED_SORTS.contains(sortKey)) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Tham số sắp xếp không hợp lệ: " + sort);
        }

        Sort sortOrder = buildSort(sortKey);
        Pageable pageable = PageRequest.of(Math.max(0, page), size, sortOrder);

        Specification<Book> spec = BookSpecification.buildSpecification(filter, BookStatus.ACTIVE);
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        List<BookCardResponse> items = bookPage.getContent().stream()
                .map(this::mapToBookCard)
                .toList();

        PageMeta pageMeta = PageMeta.builder()
                .number(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .first(bookPage.isFirst())
                .last(bookPage.isLast())
                .build();

        BookFacetsResponse facets = computeFacets(bookPage.getContent());

        return BookSearchResponse.builder()
                .items(items)
                .page(pageMeta)
                .facets(facets)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCardResponse> getBestSellers(int limit, String categorySlug) {
        int finalLimit = Math.min(Math.max(1, limit), 50);
        Pageable pageable = PageRequest.of(0, finalLimit);
        List<Book> books;
        if (StringUtils.hasText(categorySlug)) {
            books = bookRepository.findTopBestSellersByCategory(categorySlug.trim(), pageable);
        } else {
            books = bookRepository.findTopBestSellers(pageable);
        }
        return books.stream().map(this::mapToBookCard).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCardResponse> getNewArrivals(int limit, String categorySlug) {
        int finalLimit = Math.min(Math.max(1, limit), 50);
        Pageable pageable = PageRequest.of(0, finalLimit);
        List<Book> books;
        if (StringUtils.hasText(categorySlug)) {
            books = bookRepository.findNewArrivalsByCategory(categorySlug.trim(), pageable);
        } else {
            books = bookRepository.findNewArrivals(pageable);
        }
        return books.stream().map(this::mapToBookCard).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookSuggestResponse suggest(String keyword) {
        if (!StringUtils.hasText(keyword) || keyword.trim().length() < 2) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Từ khóa tìm kiếm phải có ít nhất 2 ký tự");
        }
        String cleanKeyword = keyword.trim();

        List<Book> books = bookRepository.searchAutocompleteBooks(cleanKeyword, PageRequest.of(0, 5));
        List<BookSuggestItem> bookSuggestItems = books.stream()
                .map(b -> BookSuggestItem.builder()
                        .id(b.getId())
                        .title(b.getTitle())
                        .slug(b.getSlug())
                        .thumbnailUrl(b.getThumbnailUrl())
                        .salePrice(b.getSalePrice())
                        .authors(mapAuthors(b.getAuthors()))
                        .build())
                .toList();

        List<Author> authors = authorRepository.findTop10ByNameContainingIgnoreCase(cleanKeyword);
        List<AuthorSummaryResponse> authorSummaries = authors.stream()
                .limit(3)
                .map(a -> AuthorSummaryResponse.builder().id(a.getId()).name(a.getName()).slug(a.getSlug()).build())
                .toList();

        List<Category> categories = categoryRepository.findTop10ByNameContainingIgnoreCase(cleanKeyword);
        List<CategorySummaryResponse> categorySummaries = categories.stream()
                .limit(3)
                .map(c -> CategorySummaryResponse.builder().id(c.getId()).name(c.getName()).slug(c.getSlug()).build())
                .toList();

        List<String> keywords = new ArrayList<>();
        keywords.add(cleanKeyword);
        for (Book b : books) {
            if (keywords.size() >= 5) break;
            if (!keywords.contains(b.getTitle())) {
                keywords.add(b.getTitle());
            }
        }

        return BookSuggestResponse.builder()
                .books(bookSuggestItems)
                .authors(authorSummaries)
                .categories(categorySummaries)
                .keywords(keywords)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailResponse getBookDetail(String slug, Long currentAccountId) {
        Book book = bookRepository.findBySlugAndStatus(slug, BookStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        boolean isInWishlist = false;
        if (currentAccountId != null) {
            Customer customer = customerRepository.findByAccountId(currentAccountId).orElse(null);
            if (customer != null) {
                isInWishlist = wishlistRepository.existsByIdCustomerIdAndIdBookId(customer.getId(), book.getId());
            }
        }

        List<CategoryBreadcrumbItem> breadcrumb = buildBreadcrumb(book.getCategory());
        RatingSummaryResponse ratingSummary = buildRatingSummary(book);

        List<BookImageResponse> images = book.getImages() != null
                ? book.getImages().stream()
                .map(img -> BookImageResponse.builder().id(img.getId()).url(img.getUrl()).sortOrder(img.getSortOrder()).build())
                .toList()
                : Collections.emptyList();

        CategorySummaryResponse categorySummary = CategorySummaryResponse.builder()
                .id(book.getCategory().getId())
                .name(book.getCategory().getName())
                .slug(book.getCategory().getSlug())
                .build();

        PublisherSummaryResponse publisherSummary = PublisherSummaryResponse.builder()
                .id(book.getPublisher().getId())
                .name(book.getPublisher().getName())
                .slug(book.getPublisher().getSlug())
                .build();

        return BookDetailResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .slug(book.getSlug())
                .originalPrice(book.getOriginalPrice())
                .salePrice(book.getSalePrice())
                .discountPercent(book.getDiscountPercent())
                .stockStatus(book.getStockStatus())
                .maxPurchasableQuantity(book.getMaxPurchasableQuantity())
                .isBestSeller(book.getSoldCount() >= 1000)
                .isInWishlist(isInWishlist)
                .authors(mapAuthors(book.getAuthors()))
                .translator(book.getTranslator())
                .publisher(publisherSummary)
                .category(categorySummary)
                .breadcrumb(breadcrumb)
                .publicationYear(book.getPublicationYear())
                .language(book.getLanguage())
                .pageCount(book.getPageCount())
                .coverType(book.getCoverType())
                .dimensions(book.getDimensions())
                .weightGram(book.getWeightGram())
                .images(images)
                .description(book.getDescription())
                .ratingSummary(ratingSummary)
                .soldCount(book.getSoldCount())
                .createdAt(book.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCardResponse> getRelatedBooks(String slug, int limit) {
        Book book = bookRepository.findBySlugAndStatus(slug, BookStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        int finalLimit = Math.min(Math.max(1, limit), 20);
        Pageable pageable = PageRequest.of(0, finalLimit);

        Set<Long> collectedBookIds = new HashSet<>();
        List<Book> result = new ArrayList<>();

        // 1. Same author(s)
        List<Long> authorIds = book.getAuthors() != null
                ? book.getAuthors().stream().map(author -> author.getId()).toList()
                : Collections.emptyList();

        if (!authorIds.isEmpty()) {
            List<Book> sameAuthorBooks = bookRepository.findRelatedBooksByAuthors(authorIds, book.getId(), pageable);
            for (Book b : sameAuthorBooks) {
                if (result.size() < finalLimit && collectedBookIds.add(b.getId())) {
                    result.add(b);
                }
            }
        }

        // 2. Same category
        if (result.size() < finalLimit && book.getCategory() != null) {
            List<Book> sameCategoryBooks = bookRepository.findRelatedBooksByCategory(book.getCategory().getId(), book.getId(), pageable);
            for (Book b : sameCategoryBooks) {
                if (result.size() < finalLimit && collectedBookIds.add(b.getId())) {
                    result.add(b);
                }
            }
        }

        // 3. Same publisher
        if (result.size() < finalLimit && book.getPublisher() != null) {
            List<Book> samePubBooks = bookRepository.findRelatedBooksByPublisher(book.getPublisher().getId(), book.getId(), pageable);
            for (Book b : samePubBooks) {
                if (result.size() < finalLimit && collectedBookIds.add(b.getId())) {
                    result.add(b);
                }
            }
        }

        return result.stream().map(this::mapToBookCard).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookReviewResponse> getBookReviews(String slug, Integer rating, Boolean hasContent, int page, int size, String sort) {
        Book book = bookRepository.findBySlugAndStatus(slug, BookStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        Sort sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");
        if (StringUtils.hasText(sort)) {
            String[] parts = sort.split(",");
            if (parts.length == 2 && "asc".equalsIgnoreCase(parts[1])) {
                sortOrder = Sort.by(Sort.Direction.ASC, parts[0].trim());
            } else if (parts.length >= 1) {
                sortOrder = Sort.by(Sort.Direction.DESC, parts[0].trim());
            }
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 50), sortOrder);
        Page<Review> reviewPage;

        boolean filterContent = Boolean.TRUE.equals(hasContent);
        if (rating != null && rating >= 1 && rating <= 5) {
            if (filterContent) {
                reviewPage = reviewRepository.findReviewsWithContentAndRating(book.getId(), ReviewStatus.VISIBLE, rating, pageable);
            } else {
                reviewPage = reviewRepository.findByBookIdAndStatusAndRating(book.getId(), ReviewStatus.VISIBLE, rating, pageable);
            }
        } else {
            if (filterContent) {
                reviewPage = reviewRepository.findReviewsWithContent(book.getId(), ReviewStatus.VISIBLE, pageable);
            } else {
                reviewPage = reviewRepository.findByBookIdAndStatus(book.getId(), ReviewStatus.VISIBLE, pageable);
            }
        }

        List<BookReviewResponse> items = reviewPage.getContent().stream()
                .map(r -> {
                    ReviewCustomerResponse customerResp = ReviewCustomerResponse.builder()
                            .displayName(MaskingUtils.maskName(r.getCustomer().getFullName()))
                            .avatarUrl(r.getCustomer().getAvatarUrl())
                            .tier(r.getCustomer().getCustomerTier())
                            .build();

                    ReviewAdminReplyResponse adminReplyResp = null;
                    if (StringUtils.hasText(r.getAdminReply())) {
                        adminReplyResp = ReviewAdminReplyResponse.builder()
                                .content(r.getAdminReply())
                                .repliedAt(r.getRepliedAt())
                                .build();
                    }

                    return BookReviewResponse.builder()
                            .id(r.getId())
                            .rating(r.getRating())
                            .content(r.getContent())
                            .customer(customerResp)
                            .isVerifiedPurchase(true)
                            .adminReply(adminReplyResp)
                            .createdAt(r.getCreatedAt())
                            .build();
                })
                .toList();

        PageMeta meta = PageMeta.builder()
                .number(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .first(reviewPage.isFirst())
                .last(reviewPage.isLast())
                .build();

        return PageResponse.of(items, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminBookResponse> getAdminBooks(String keyword, String status, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 60), Sort.by(Sort.Direction.DESC, "createdAt"));

        BookStatus bookStatus = null;
        if (StringUtils.hasText(status)) {
            try {
                bookStatus = BookStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        BookSearchFilter filter = BookSearchFilter.builder()
                .keyword(keyword)
                .build();

        Specification<Book> spec = BookSpecification.buildSpecification(filter, bookStatus);
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        List<AdminBookResponse> items = bookPage.getContent().stream()
                .map(b -> AdminBookResponse.builder()
                        .id(b.getId())
                        .isbn(b.getIsbn())
                        .title(b.getTitle())
                        .slug(b.getSlug())
                        .thumbnailUrl(b.getThumbnailUrl())
                        .authors(mapAuthors(b.getAuthors()))
                        .originalPrice(b.getOriginalPrice())
                        .salePrice(b.getSalePrice())
                        .stockQuantity(b.getStockQuantity())
                        .status(b.getStatus())
                        .soldCount(b.getSoldCount())
                        .createdAt(b.getCreatedAt())
                        .version(b.getVersion())
                        .build())
                .toList();

        PageMeta meta = PageMeta.builder()
                .number(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .first(bookPage.isFirst())
                .last(bookPage.isLast())
                .build();

        return PageResponse.of(items, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminBookDetailResponse getAdminBookDetail(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        List<BookImageResponse> images = book.getImages() != null
                ? book.getImages().stream()
                .map(img -> BookImageResponse.builder().id(img.getId()).url(img.getUrl()).sortOrder(img.getSortOrder()).build())
                .toList()
                : Collections.emptyList();

        CategorySummaryResponse categorySummary = CategorySummaryResponse.builder()
                .id(book.getCategory().getId())
                .name(book.getCategory().getName())
                .slug(book.getCategory().getSlug())
                .build();

        PublisherSummaryResponse publisherSummary = PublisherSummaryResponse.builder()
                .id(book.getPublisher().getId())
                .name(book.getPublisher().getName())
                .slug(book.getPublisher().getSlug())
                .build();

        return AdminBookDetailResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .slug(book.getSlug())
                .originalPrice(book.getOriginalPrice())
                .salePrice(book.getSalePrice())
                .stockQuantity(book.getStockQuantity())
                .lowStockThreshold(book.getLowStockThreshold())
                .status(book.getStatus())
                .authors(mapAuthors(book.getAuthors()))
                .translator(book.getTranslator())
                .publisher(publisherSummary)
                .category(categorySummary)
                .publicationYear(book.getPublicationYear())
                .language(book.getLanguage())
                .pageCount(book.getPageCount())
                .coverType(book.getCoverType())
                .dimensions(book.getDimensions())
                .weightGram(book.getWeightGram())
                .images(images)
                .description(book.getDescription())
                .soldCount(book.getSoldCount())
                .version(book.getVersion())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    private Sort buildSort(String sortKey) {
        return switch (sortKey) {
            case "saleprice,asc" -> Sort.by(Sort.Direction.ASC, "salePrice");
            case "saleprice,desc" -> Sort.by(Sort.Direction.DESC, "salePrice");
            case "soldcount,desc", "relevance" -> Sort.by(Sort.Direction.DESC, "soldCount");
            case "avgrating,desc" -> Sort.by(Sort.Direction.DESC, "avgRating");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private BookCardResponse mapToBookCard(Book book) {
        return BookCardResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .slug(book.getSlug())
                .thumbnailUrl(book.getThumbnailUrl())
                .authors(mapAuthors(book.getAuthors()))
                .originalPrice(book.getOriginalPrice())
                .salePrice(book.getSalePrice())
                .discountPercent(book.getDiscountPercent())
                .avgRating(book.getAvgRating())
                .reviewCount(book.getReviewCount())
                .soldCount(book.getSoldCount())
                .stockStatus(book.getStockStatus())
                .isBestSeller(book.getSoldCount() >= 1000)
                .build();
    }

    private List<AuthorSummaryResponse> mapAuthors(List<Author> authors) {
        if (authors == null || authors.isEmpty()) {
            return Collections.emptyList();
        }
        return authors.stream()
                .map(a -> AuthorSummaryResponse.builder().id(a.getId()).name(a.getName()).slug(a.getSlug()).build())
                .toList();
    }

    private List<CategoryBreadcrumbItem> buildBreadcrumb(Category category) {
        List<CategoryBreadcrumbItem> breadcrumb = new ArrayList<>();
        Category current = category;
        while (current != null) {
            breadcrumb.add(CategoryBreadcrumbItem.builder()
                    .id(current.getId())
                    .name(current.getName())
                    .slug(current.getSlug())
                    .build());
            current = current.getParent();
        }
        Collections.reverse(breadcrumb);
        return breadcrumb;
    }

    private RatingSummaryResponse buildRatingSummary(Book book) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("5", 0);
        distribution.put("4", 0);
        distribution.put("3", 0);
        distribution.put("2", 0);
        distribution.put("1", 0);

        List<Object[]> rows = reviewRepository.countRatingsByStar(book.getId());
        for (Object[] row : rows) {
            Integer star = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            distribution.put(String.valueOf(star), count.intValue());
        }

        return RatingSummaryResponse.builder()
                .avgRating(book.getAvgRating())
                .reviewCount(book.getReviewCount())
                .distribution(distribution)
                .build();
    }

    private BookFacetsResponse computeFacets(List<Book> books) {
        Map<String, Long> langMap = new HashMap<>();
        Map<String, Long> coverMap = new HashMap<>();
        long range1 = 0; // 0 - 100,000
        long range2 = 0; // 100,000 - 200,000
        long range3 = 0; // > 200,000

        for (Book b : books) {
            if (StringUtils.hasText(b.getLanguage())) {
                langMap.put(b.getLanguage().toUpperCase(), langMap.getOrDefault(b.getLanguage().toUpperCase(), 0L) + 1);
            }
            if (b.getCoverType() != null) {
                coverMap.put(b.getCoverType().name(), coverMap.getOrDefault(b.getCoverType().name(), 0L) + 1);
            }
            if (b.getSalePrice() != null) {
                if (b.getSalePrice() <= 100000) {
                    range1++;
                } else if (b.getSalePrice() <= 200000) {
                    range2++;
                } else {
                    range3++;
                }
            }
        }

        List<FacetItemResponse> languages = langMap.entrySet().stream()
                .map(e -> FacetItemResponse.builder().value(e.getKey()).count(e.getValue()).build())
                .toList();

        List<FacetItemResponse> coverTypes = coverMap.entrySet().stream()
                .map(e -> FacetItemResponse.builder().value(e.getKey()).count(e.getValue()).build())
                .toList();

        List<PriceRangeFacetResponse> priceRanges = List.of(
                PriceRangeFacetResponse.builder().from(0L).to(100000L).count(range1).build(),
                PriceRangeFacetResponse.builder().from(100000L).to(200000L).count(range2).build(),
                PriceRangeFacetResponse.builder().from(200000L).to(null).count(range3).build()
        );

        return BookFacetsResponse.builder()
                .languages(languages)
                .coverTypes(coverTypes)
                .priceRanges(priceRanges)
                .build();
    }
}
