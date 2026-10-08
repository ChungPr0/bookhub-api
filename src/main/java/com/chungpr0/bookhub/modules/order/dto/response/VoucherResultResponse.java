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
public class VoucherResultResponse {

    @Schema(description = "Trạng thái áp dụng thành công hay không", example = "true")
    private boolean applied;

    @Schema(description = "Mã voucher đã kiểm tra", example = "BOOKHUB10")
    private String code;

    @Schema(description = "Mã lỗi nếu áp dụng thất bại (null nếu thành công)", example = "VOUCHER_MIN_ORDER_NOT_MET")
    private String errorCode;

    @Schema(description = "Thông báo chi tiết lỗi", example = "Đơn hàng chưa đạt giá trị tối thiểu 200.000đ")
    private String errorMessage;
}

