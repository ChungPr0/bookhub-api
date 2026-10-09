package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateReviewRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateReviewRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.CustomerReviewItemResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PendingReviewItemResponse;
import com.chungpr0.bookhub.modules.catalog.service.ReviewService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "6.4 Đánh giá của Khách hàng (Customer Reviews)", description = "APIs quản lý và gửi đánh giá sản phẩm sách đã mua dành cho Độc giả")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class CustomerReviewController {

    private final ReviewService reviewService;

    @GetMapping("/pending")
    @Operation(
            summary = "REV-01: Danh sách Sách đã mua chờ đánh giá",
            description = "Hiển thị danh sách các cuốn sách thuộc các đơn hàng COMPLETED trong 30 ngày qua mà khách hàng chưa viết đánh giá."
    )
    public ResponseEntity<ApiResponse<PageResponse<PendingReviewItemResponse>>> getPendingReviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PendingReviewItemResponse> response = reviewService.getPendingReviews(principal.getAccountId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sách chờ đánh giá thành công", response));
    }

    @GetMapping
    @Operation(
            summary = "REV-02: Lịch sử đánh giá của tôi",
            description = "Xem danh sách các đánh giá đã gửi kèm theo thông tin phản hồi từ phía cửa hàng và hạn chót cho phép chỉnh sửa."
    )
    public ResponseEntity<ApiResponse<PageResponse<CustomerReviewItemResponse>>> getCustomerReviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<CustomerReviewItemResponse> response = reviewService.getCustomerReviews(principal.getAccountId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá của tôi thành công", response));
    }

    @PostMapping
    @Operation(
            summary = "REV-03: Viết đánh giá mới",
            description = "Gửi đánh giá và nhận xét cho một cuốn sách trong đơn hàng đã hoàn tất (trong vòng 30 ngày)."
    )
    public ResponseEntity<ApiResponse<CustomerReviewItemResponse>> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        CustomerReviewItemResponse response = reviewService.createReview(principal.getAccountId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng bài đánh giá thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "REV-04: Chỉnh sửa đánh giá",
            description = "Chỉnh sửa số sao và nội dung bài đánh giá của chính mình trong thời hạn 30 ngày kể từ ngày viết."
    )
    public ResponseEntity<ApiResponse<CustomerReviewItemResponse>> updateReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID bài đánh giá", example = "55", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        CustomerReviewItemResponse response = reviewService.updateReview(principal.getAccountId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài đánh giá thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "REV-05: Xóa bài đánh giá",
            description = "Xóa bài đánh giá của chính mình trong thời hạn 30 ngày kể từ ngày tạo bài."
    )
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID bài đánh giá", example = "55", required = true)
            @PathVariable Long id
    ) {
        reviewService.deleteReview(principal.getAccountId(), id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài đánh giá thành công", null));
    }
}

