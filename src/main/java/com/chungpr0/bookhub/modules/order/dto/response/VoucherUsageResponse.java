package com.chungpr0.bookhub.modules.order.dto.response;

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
public class VoucherUsageResponse {

    @Schema(description = "ID bản ghi sử dụng", example = "101")
    private Long id;

    @Schema(description = "Mã voucher", example = "BOOKHUB10K")
    private String voucherCode;

    @Schema(description = "ID đơn hàng áp dụng voucher", example = "8801")
    private Long orderId;

    @Schema(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @Schema(description = "ID khách hàng", example = "1001")
    private Long customerId;

    @Schema(description = "Tên khách hàng", example = "Nguyễn Tiến Chung")
    private String customerName;

    @Schema(description = "Số tiền đã giảm (VND)", example = "10000")
    private Long discountAmount;

    @Schema(description = "Thời gian sử dụng mã")
    private OffsetDateTime usedAt;

    @Schema(description = "Thời gian hoàn lại mã nếu đơn bị hủy")
    private OffsetDateTime releasedAt;
}

