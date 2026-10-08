package com.chungpr0.bookhub.modules.order.dto.request;

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
public class ShippingCalculateItemRequest {

    @Schema(description = "ID cuốn sách cần tính cước", example = "101")
    @NotNull(message = "ID sách không được để trống")
    private Long bookId;

    @Schema(description = "Số lượng đặt mua", example = "2")
    @NotNull(message = "Số lượng sách không được để trống")
    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    @Max(value = 99, message = "Số lượng tối đa là 99")
    private Integer quantity;
}

