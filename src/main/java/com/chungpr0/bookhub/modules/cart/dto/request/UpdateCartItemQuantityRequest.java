package com.chungpr0.bookhub.modules.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Payload cập nhật số lượng sách trong giỏ")
public class UpdateCartItemQuantityRequest {

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải từ 1 đến 99")
    @Max(value = 99, message = "Số lượng phải từ 1 đến 99")
    @Schema(description = "Số lượng chốt cuối cùng của sách trong giỏ (1 - 99)", example = "3")
    private Integer quantity;
}

