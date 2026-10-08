package com.chungpr0.bookhub.modules.catalog.dto.response;

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
@Schema(description = "Thông tin nhận xét đánh giá của độc giả")
public class BookReviewResponse {

    @Schema(description = "ID đánh giá", example = "55")
    private Long id;

    @Schema(description = "Số sao đánh giá (1-5)", example = "5")
    private int rating;

    @Schema(description = "Nội dung nhận xét", example = "Sách bọc cẩn thận, bìa đẹp, in rõ nét.")
    private String content;

    @Schema(description = "Khách hàng đánh giá")
    private ReviewCustomerResponse customer;

    @com.fasterxml.jackson.annotation.JsonProperty("isVerifiedPurchase")
    @Schema(description = "Xác nhận đã mua hàng thực tế", example = "true")
    private boolean isVerifiedPurchase;

    @Schema(description = "Phản hồi từ quản trị viên")
    private ReviewAdminReplyResponse adminReply;

    @Schema(description = "Thời gian gửi đánh giá", example = "2026-10-05T15:00:00+07:00")
    private OffsetDateTime createdAt;
}

