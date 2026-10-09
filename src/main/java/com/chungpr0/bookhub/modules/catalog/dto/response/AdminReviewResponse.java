package com.chungpr0.bookhub.modules.catalog.dto.response;

import com.chungpr0.bookhub.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mục đánh giá sản phẩm dành cho Quản trị viên (ARV-01)")
public class AdminReviewResponse {

    @Schema(description = "ID bài đánh giá", example = "89")
    private Long id;

    @Schema(description = "ID đơn hàng", example = "8801")
    private Long orderId;

    @Schema(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @Schema(description = "Thông tin khách hàng đánh giá")
    private ReviewCustomerInfoResponse customer;

    @Schema(description = "Thông tin cuốn sách được đánh giá")
    private BookSummaryItemResponse book;

    @Schema(description = "Số sao đánh giá (1 - 5)", example = "5")
    private int rating;

    @Schema(description = "Nội dung nhận xét chi tiết", example = "Sách bọc màng co cẩn thận...")
    private String content;

    @Schema(description = "Trạng thái hiển thị (VISIBLE, HIDDEN)", example = "VISIBLE")
    private ReviewStatus status;

    @Schema(description = "Lý do ẩn (nếu bị kiểm duyệt)", example = "Chứa từ ngữ xúc phạm")
    private String hiddenReason;

    @Schema(description = "Phản hồi từ cửa hàng")
    private ReviewAdminReplyResponse adminReply;

    @Schema(description = "Thời gian tạo đánh giá", example = "2026-10-07T17:45:56+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật đánh giá", example = "2026-10-07T17:45:56+07:00")
    private OffsetDateTime updatedAt;
}

