package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.common.enums.PointTransactionType;
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
public class PointTransactionResponse {

    @Schema(description = "ID giao dịch điểm", example = "901")
    private Long id;

    @Schema(description = "Loại giao dịch điểm", example = "EARN")
    private PointTransactionType type;

    @Schema(description = "Số điểm biến động (dương = cộng, âm = trừ)", example = "38")
    private int points;

    @Schema(description = "Số dư điểm sau giao dịch", example = "1420")
    private int balanceAfter;

    @Schema(description = "Mã đơn hàng liên quan (nếu có)", example = "101")
    private Long orderId;

    @Schema(description = "Ghi chú giao dịch", example = "Tích điểm từ đơn hàng")
    private String note;

    @Schema(description = "Thời gian giao dịch", example = "2026-10-05T14:30:00+07:00")
    private OffsetDateTime createdAt;
}

