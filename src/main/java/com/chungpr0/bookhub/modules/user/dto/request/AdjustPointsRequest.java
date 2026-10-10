package com.chungpr0.bookhub.modules.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class AdjustPointsRequest {

    @NotNull(message = "Số điểm điều chỉnh không được để trống")
    @Schema(description = "Số điểm cần điều chỉnh (dương để cộng bù, âm để trừ phạt)", example = "500")
    private Integer points;

    @NotBlank(message = "Lý do điều chỉnh không được để trống")
    @Size(min = 5, max = 255, message = "Lý do điều chỉnh phải từ 5 đến 255 ký tự")
    @Schema(description = "Lý do điều chỉnh điểm thưởng", example = "Tặng điểm đền bù cho khách hàng do đơn hàng bị chậm trễ vận chuyển 3 ngày")
    private String reason;
}

