package com.chungpr0.bookhub.modules.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPreviewResponse {

    @Schema(description = "Danh sách chi tiết các cuốn sách trong đơn")
    @Builder.Default
    private List<CheckoutItemResponse> items = new ArrayList<>();

    @Schema(description = "Bảng kê khai chi tiết giá cả và các khoản giảm trừ")
    private OrderPricingResponse pricing;

    @Schema(description = "Thông tin điểm thưởng của khách hàng")
    private PointsPreviewResponse points;

    @Schema(description = "Kết quả áp dụng mã voucher")
    private VoucherResultResponse voucherResult;

    @Schema(description = "Các cảnh báo về tồn kho hoặc điều kiện áp dụng")
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}

