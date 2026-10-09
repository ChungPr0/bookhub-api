package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewReplyRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewVisibilityRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminReviewResponse;
import com.chungpr0.bookhub.modules.catalog.service.ReviewService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "6.5 Kiểm duyệt Đánh giá (Admin Review Moderation)", description = "APIs quản trị, kiểm duyệt nội dung và phản hồi đánh giá độc giả")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(
            summary = "ARV-01: Danh sách đánh giá Admin",
            description = "Tra cứu và lọc toàn bộ đánh giá theo từ khóa, đầu sách, số sao, trạng thái kiểm duyệt và tình trạng phản hồi của shop."
    )
    public ResponseEntity<ApiResponse<PageResponse<AdminReviewResponse>>> getAdminReviews(
            @Parameter(description = "Từ khóa tìm kiếm (nội dung, tên sách, tên hoặc SĐT khách)", example = "hay")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo ID sách", example = "101")
            @RequestParam(required = false) Long bookId,
            @Parameter(description = "Lọc theo số sao đánh giá (1 - 5)", example = "5")
            @RequestParam(required = false) Integer rating,
            @Parameter(description = "Lọc theo trạng thái hiển thị (VISIBLE, HIDDEN)", example = "VISIBLE")
            @RequestParam(required = false) ReviewStatus status,
            @Parameter(description = "Lọc theo tình trạng phản hồi (true = đã trả lời, false = chưa trả lời)", example = "false")
            @RequestParam(required = false) Boolean hasReply,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang (tối đa 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tiêu chí sắp xếp", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        PageResponse<AdminReviewResponse> response = reviewService.getAdminReviews(keyword, bookId, rating, status, hasReply, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công", response));
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ARV-02: Ẩn / Mở lại hiển thị đánh giá (Kiểm duyệt)",
            description = "Kiểm duyệt ẩn hoặc hiện đánh giá (khi ẩn bắt buộc nhập lý do). Tự động loại trừ đánh giá ẩn khỏi điểm trung bình của sách."
    )
    public ResponseEntity<ApiResponse<Void>> updateReviewVisibility(
            @Parameter(description = "ID bài đánh giá", example = "89", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AdminReviewVisibilityRequest request
    ) {
        reviewService.updateReviewVisibility(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái kiểm duyệt thành công", null));
    }

    @PutMapping("/{id}/reply")
    @Operation(
            summary = "ARV-03: Trả lời nhận xét của khách hàng",
            description = "Đăng phản hồi chính thức của Cửa hàng BookHub đối với nhận xét của khách hàng."
    )
    public ResponseEntity<ApiResponse<Void>> replyReview(
            @Parameter(description = "ID bài đánh giá", example = "89", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AdminReviewReplyRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long staffAccountId = principal != null ? principal.getAccountId() : null;
        reviewService.replyReview(id, request, staffAccountId);
        return ResponseEntity.ok(ApiResponse.success("Phản hồi đánh giá thành công", null));
    }

    @DeleteMapping("/{id}/reply")
    @Operation(
            summary = "ARV-04: Xóa câu trả lời của cửa hàng",
            description = "Xóa nội dung phản hồi của cửa hàng đối với bài đánh giá."
    )
    public ResponseEntity<ApiResponse<Void>> deleteReviewReply(
            @Parameter(description = "ID bài đánh giá", example = "89", required = true)
            @PathVariable Long id
    ) {
        reviewService.deleteReviewReply(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa câu phản hồi thành công", null));
    }
}

