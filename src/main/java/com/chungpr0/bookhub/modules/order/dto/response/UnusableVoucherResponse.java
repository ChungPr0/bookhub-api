package com.chungpr0.bookhub.modules.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnusableVoucherResponse {

    @Schema(description = "Thông tin cơ bản của voucher")
    private VoucherResponse voucher;

    @Schema(description = "Mã lý do không thể áp dụng", example = "VOUCHER_MIN_ORDER_NOT_MET")
    private String reasonCode;

    @Schema(description = "Thông báo lý do dễ hiểu cho người dùng", example = "Đơn hàng chưa đạt giá trị tối thiểu 500.000đ (còn thiếu 250.000đ)")
    private String reasonMessage;
}

