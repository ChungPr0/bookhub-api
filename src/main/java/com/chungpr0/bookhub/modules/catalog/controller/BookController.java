package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.request.BookSearchFilter;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookReviewResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSearchResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSuggestResponse;
import com.chungpr0.bookhub.modules.catalog.service.BookService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "3.2 Khám phá Sách (Books Discovery)", description = "APIs tìm kiếm, lọc đa chiều, gợi ý tự động, top bán chạy và xem chi tiết sản phẩm sách")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(
            summary = "BOK-01: Tìm kiếm & Lọc sách đa tiêu chí",
            description = "Tìm kiếm và lọc sách động bằng JPA Specification theo từ khóa, danh mục, tác giả, NXB, ngôn ngữ, khoảng giá, đánh giá và trạng thái tồn kho."
    )
    public ResponseEntity<ApiResponse<BookSearchResponse>> searchBooks(
            @Parameter(description = "Từ khóa tìm kiếm (tiêu đề, ISBN hoặc tên tác giả)", example = "Nhà Giả Kim")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Slug danh mục", example = "van-hoc")
            @RequestParam(required = false) String categorySlug,
            @Parameter(description = "Slug tác giả", example = "paulo-coelho")
            @RequestParam(required = false) String authorSlug,
            @Parameter(description = "Slug nhà xuất bản", example = "nxb-hoi-nha-van")
            @RequestParam(required = false) String publisherSlug,
            @Parameter(description = "Mã ngôn ngữ (VI, EN...)", example = "VI")
            @RequestParam(required = false) String language,
            @Parameter(description = "Hình thức bìa (PAPERBACK, HARDCOVER)", example = "PAPERBACK")
            @RequestParam(required = false) String coverType,
            @Parameter(description = "Mức giá tối thiểu (VND)", example = "50000")
            @RequestParam(required = false) Long priceFrom,
            @Parameter(description = "Mức giá tối đa (VND)", example = "200000")
            @RequestParam(required = false) Long priceTo,
            @Parameter(description = "Đánh giá tối thiểu từ 1 đến 5 sao", example = "4")
            @RequestParam(required = false) Integer minRating,
            @Parameter(description = "Chỉ lấy sách còn hàng trong kho", example = "false")
            @RequestParam(defaultValue = "false") Boolean inStockOnly,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang (1 - 60)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tiêu chí sắp xếp (relevance, createdAt,desc, salePrice,asc, salePrice,desc, soldCount,desc, avgRating,desc)", example = "createdAt,desc")
            @RequestParam(required = false) String sort
    ) {
        BookSearchFilter filter = BookSearchFilter.builder()
                .keyword(keyword)
                .categorySlug(categorySlug)
                .authorSlug(authorSlug)
                .publisherSlug(publisherSlug)
                .language(language)
                .coverType(coverType)
                .priceFrom(priceFrom)
                .priceTo(priceTo)
                .minRating(minRating)
                .inStockOnly(inStockOnly)
                .build();

        BookSearchResponse response = bookService.searchBooks(filter, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sách thành công", response));
    }

    @GetMapping("/best-sellers")
    @Operation(
            summary = "BOK-02: Danh sách Sách bán chạy nhất",
            description = "Hiển thị danh sách sách bán chạy nhất sắp xếp theo số lượng đã bán (soldCount DESC)."
    )
    public ResponseEntity<ApiResponse<List<BookCardResponse>>> getBestSellers(
            @Parameter(description = "Số lượng giới hạn (1 - 50)", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Lọc theo danh mục cụ thể", example = "van-hoc")
            @RequestParam(required = false) String categorySlug
    ) {
        List<BookCardResponse> response = bookService.getBestSellers(limit, categorySlug);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sách bán chạy thành công", response));
    }

    @GetMapping("/new-arrivals")
    @Operation(
            summary = "BOK-03: Danh sách Sách mới phát hành",
            description = "Hiển thị danh sách các cuốn sách mới xuất bản sắp xếp theo ngày tạo mới nhất (createdAt DESC)."
    )
    public ResponseEntity<ApiResponse<List<BookCardResponse>>> getNewArrivals(
            @Parameter(description = "Số lượng giới hạn (1 - 50)", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Lọc theo danh mục cụ thể", example = "van-hoc")
            @RequestParam(required = false) String categorySlug
    ) {
        List<BookCardResponse> response = bookService.getNewArrivals(limit, categorySlug);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sách mới phát hành thành công", response));
    }

    @GetMapping("/suggest")
    @Operation(
            summary = "BOK-04: Gợi ý tìm kiếm nhanh (Autocomplete)",
            description = "Trả về kết quả gợi ý tức thời gồm tối đa 5 đầu sách, danh sách tác giả, danh mục và từ khóa phù hợp với chuỗi tìm kiếm."
    )
    public ResponseEntity<ApiResponse<BookSuggestResponse>> suggest(
            @Parameter(description = "Từ khóa tìm kiếm (tối thiểu 2 ký tự)", example = "Nhà Giả Kim", required = true)
            @RequestParam String q
    ) {
        BookSuggestResponse response = bookService.suggest(q);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu gợi ý thành công", response));
    }

    @GetMapping("/{slug}")
    @Operation(
            summary = "BOK-05: Chi tiết sách (Core Book Detail)",
            description = "Hiển thị đầy đủ thông tin chi tiết một cuốn sách. Nếu có Access Token hợp lệ, hệ thống trả thêm trạng thái yêu thích (isInWishlist)."
    )
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBookDetail(
            @Parameter(description = "Slug cuốn sách", example = "nha-gia-kim", required = true)
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long accountId = principal != null ? principal.getAccountId() : null;
        BookDetailResponse response = bookService.getBookDetail(slug, accountId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sách thành công", response));
    }

    @GetMapping("/{slug}/related")
    @Operation(
            summary = "BOK-06: Danh sách Sách liên quan",
            description = "Gợi ý danh sách các cuốn sách liên quan ưu tiên theo: Cùng tác giả -> Cùng danh mục -> Cùng NXB, loại trừ cuốn sách hiện tại."
    )
    public ResponseEntity<ApiResponse<List<BookCardResponse>>> getRelatedBooks(
            @Parameter(description = "Slug cuốn sách hiện tại", example = "nha-gia-kim", required = true)
            @PathVariable String slug,
            @Parameter(description = "Số lượng giới hạn (tối đa 20)", example = "8")
            @RequestParam(defaultValue = "8") int limit
    ) {
        List<BookCardResponse> response = bookService.getRelatedBooks(slug, limit);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sách liên quan thành công", response));
    }

    @GetMapping("/{slug}/reviews")
    @Operation(
            summary = "BOK-07: Danh sách Đánh giá của độc giả",
            description = "Lấy danh sách nhận xét đánh giá công khai từ khách hàng. Tên khách hàng được ẩn danh bảo mật dạng 'Nguyễn T. C.'."
    )
    public ResponseEntity<ApiResponse<PageResponse<BookReviewResponse>>> getBookReviews(
            @Parameter(description = "Slug cuốn sách", example = "nha-gia-kim", required = true)
            @PathVariable String slug,
            @Parameter(description = "Lọc theo số sao đánh giá (1 - 5)", example = "5")
            @RequestParam(required = false) Integer rating,
            @Parameter(description = "Chỉ lấy đánh giá có nội dung nhận xét viết chữ", example = "true")
            @RequestParam(required = false) Boolean hasContent,
            @Parameter(description = "Số trang", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        PageResponse<BookReviewResponse> response = bookService.getBookReviews(slug, rating, hasContent, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công", response));
    }
}

