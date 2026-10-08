package com.chungpr0.bookhub.modules.order.dto.request;

import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherState;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
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
public class VoucherFilterRequest {

    @Schema(description = "Từ khóa tìm kiếm theo mã voucher hoặc tên", example = "BOOKHUB")
    private String keyword;

    @Schema(description = "Lọc theo trạng thái lưu trữ (ACTIVE, INACTIVE)", example = "ACTIVE")
    private VoucherStatus status;

    @Schema(description = "Lọc theo trạng thái tính toán (UPCOMING, ACTIVE, EXPIRED, EXHAUSTED, DISABLED)", example = "ACTIVE")
    private VoucherState state;

    @Schema(description = "Lọc theo loại giảm giá (PERCENTAGE, FIXED_AMOUNT)", example = "PERCENTAGE")
    private VoucherDiscountType discountType;
}

