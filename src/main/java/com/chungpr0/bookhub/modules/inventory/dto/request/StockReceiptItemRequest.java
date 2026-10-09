package com.chungpr0.bookhub.modules.inventory.dto.request;

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
@Schema(description = "Chi tiết một đầu sách trong phiếu nhập kho")
public class StockReceiptItemRequest {

    @NotNull(message = "ID cuốn sách không được để trống")
    @Schema(description = "ID của cuốn sách cần nhập kho", example = "101")
    private Long bookId;

    @NotNull(message = "Số lượng nhập không được để trống")
    @Min(value = 1, message = "Số lượng nhập tối thiểu là 1 cuốn")
    @Max(value = 100000, message = "Số lượng nhập tối đa là 100.000 cuốn")
    @Schema(description = "Số lượng sách nhập kho", example = "200")
    private Integer quantity;

    @NotNull(message = "Đơn giá nhập không được để trống")
    @Min(value = 1, message = "Đơn giá nhập phải lớn hơn 0 VND")
    @Schema(description = "Giá vốn nhập trên mỗi cuốn (VND)", example = "47400")
    private Long importPrice;
}

