package com.chungpr0.bookhub.modules.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class AdminReturnOrderRequest {

    @Schema(description = "Lý do tiếp nhận hoàn trả hàng từ khách", example = "Sách bị lỗi in ngược trang từ nhà xuất bản")
    @NotBlank(message = "Lý do hoàn trả không được để trống")
    private String reason;

    @Schema(description = "Có nhập trả sách lại vào kho hàng hay không", example = "true")
    @NotNull(message = "Lựa chọn hoàn kho không được để trống")
    private Boolean restock;

    @Schema(description = "Phiên bản dữ liệu (Optimistic Lock)", example = "3")
    @NotNull(message = "Phiên bản dữ liệu (version) không được để trống")
    private Integer version;
}

