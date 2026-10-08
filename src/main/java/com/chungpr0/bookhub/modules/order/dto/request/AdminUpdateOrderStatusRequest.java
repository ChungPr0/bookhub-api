package com.chungpr0.bookhub.modules.order.dto.request;

import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class AdminUpdateOrderStatusRequest {

    @Schema(description = "Trạng thái mới cần chuyển sang (CONFIRMED, SHIPPING, COMPLETED)", example = "SHIPPING")
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus status;

    @Schema(description = "Ghi chú quá trình cập nhật trạng thái", example = "Đã bàn giao đơn hàng cho bưu tá GiaoHangNhanh")
    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;

    @Schema(description = "Phiên bản dữ liệu hiện tại để kiểm tra xung đột đồng thời (Optimistic Lock)", example = "1")
    @NotNull(message = "Phiên bản dữ liệu (version) không được để trống")
    private Integer version;
}

