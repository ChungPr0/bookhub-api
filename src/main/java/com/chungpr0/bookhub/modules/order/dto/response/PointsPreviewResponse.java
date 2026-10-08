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
public class PointsPreviewResponse {

    @Schema(description = "Số điểm thưởng khả dụng của khách hàng", example = "1420")
    private int available;

    @Schema(description = "Số điểm tối đa được phép dùng cho đơn này (tối đa 50% tiền sách)", example = "1243")
    private int maxRedeemable;

    @Schema(description = "Giá trị quy đổi: 1 điểm = ? VND", example = "100")
    @Builder.Default
    private int pointValue = 100;

    @Schema(description = "Số điểm tích lũy ước tính nhận được sau khi đơn hoàn tất", example = "22")
    private int estimatedEarn;
}

