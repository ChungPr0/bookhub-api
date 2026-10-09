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
@Schema(description = "Sách đã mua thành công đang chờ đánh giá (REV-01)")
public class PendingReviewItemResponse {

    @Schema(description = "ID đơn hàng", example = "8801")
    private Long orderId;

    @Schema(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @Schema(description = "Thời gian đơn hàng hoàn tất", example = "2026-10-07T18:00:00+07:00")
    private OffsetDateTime completedAt;

    @Schema(description = "Hạn chót viết đánh giá (30 ngày kể từ khi hoàn tất)", example = "2026-11-06T18:00:00+07:00")
    private OffsetDateTime reviewDeadline;

    @Schema(description = "Thông tin tóm tắt cuốn sách")
    private BookSummaryItemResponse book;
}

