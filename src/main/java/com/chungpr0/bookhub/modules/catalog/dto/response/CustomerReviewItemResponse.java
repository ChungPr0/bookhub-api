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
@Schema(description = "Mục lịch sử đánh giá của khách hàng (REV-02)")
public class CustomerReviewItemResponse {

    @Schema(description = "ID bài đánh giá", example = "55")
    private Long id;

    @Schema(description = "Mã đơn hàng", example = "ORD-20260920-A1B2C3")
    private String orderCode;

    @Schema(description = "Thông tin tóm tắt cuốn sách")
    private BookSummaryItemResponse book;

    @Schema(description = "Số sao đánh giá (1 - 5)", example = "5")
    private int rating;

    @Schema(description = "Nội dung nhận xét chi tiết", example = "Sách rất xúc động, in đẹp, giao nhanh!")
    private String content;

    @Schema(description = "Trạng thái hiển thị (VISIBLE, HIDDEN)", example = "VISIBLE")
    private ReviewStatus status;

    @Schema(description = "Lý do bị ẩn (nếu có)", example = "null")
    private String hiddenReason;

    @Schema(description = "Cho phép chỉnh sửa hoặc xóa trong vòng 30 ngày", example = "true")
    private boolean canEdit;

    @Schema(description = "Hạn chót cho phép chỉnh sửa hoặc xóa", example = "2026-10-25T14:30:00+07:00")
    private OffsetDateTime editDeadline;

    @Schema(description = "Phản hồi chính thức từ cửa hàng")
    private ReviewAdminReplyResponse adminReply;

    @Schema(description = "Thời gian gửi đánh giá", example = "2026-09-25T14:30:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-09-25T14:30:00+07:00")
    private OffsetDateTime updatedAt;
}

