package com.chungpr0.bookhub.modules.user.dto.response;

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
public class PointAdjustmentResponse {

    @Schema(description = "ID giao dịch điểm", example = "501")
    private Long id;

    @Schema(description = "ID khách hàng", example = "1001")
    private Long customerId;

    @Schema(description = "Số điểm điều chỉnh (dương = cộng, âm = trừ)", example = "500")
    private int points;

    @Schema(description = "Số dư điểm sau khi điều chỉnh", example = "1920")
    private int balanceAfter;

    @Schema(description = "Lý do điều chỉnh điểm", example = "Tặng điểm đền bù cho khách hàng do đơn hàng bị chậm trễ vận chuyển 3 ngày")
    private String reason;

    @Schema(description = "ID tài khoản quản trị viên thực hiện", example = "1")
    private Long createdBy;

    @Schema(description = "Thời gian thực hiện điều chỉnh", example = "2026-10-09T18:00:00+07:00")
    private OffsetDateTime createdAt;
}

