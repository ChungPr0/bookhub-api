package com.chungpr0.bookhub.modules.order.dto.request;

import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @Schema(description = "Danh sách ID sách cần đặt mua", example = "[101, 105]")
    @NotEmpty(message = "Vui lòng chọn ít nhất một cuốn sách để thanh toán")
    private List<Long> bookIds;

    @Schema(description = "ID địa chỉ đã lưu trong sổ địa chỉ", example = "5")
    private Long addressId;

    @Schema(description = "Thông tin địa chỉ mới trực tiếp (nếu không dùng addressId)")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @Schema(description = "Phương thức thanh toán (COD, BANK_TRANSFER, VNPAY)", example = "VNPAY")
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethodCode paymentMethod;

    @Schema(description = "Mã voucher giảm giá muốn sử dụng", example = "BOOKHUB10")
    private String voucherCode;

    @Schema(description = "Số điểm tích lũy muốn sử dụng", example = "200")
    @Min(value = 0, message = "Số điểm thưởng sử dụng không được âm")
    @Builder.Default
    private Integer pointsToUse = 0;

    @Schema(description = "Ghi chú đơn hàng cho nhân viên hoặc bưu tá", example = "Giao giờ hành chính giúp mình")
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;

    @Schema(description = "Số tiền thanh toán cuối cùng khách hàng xác nhận từ màn hình Preview (chống trượt giá)", example = "258760")
    @NotNull(message = "Số tiền xác nhận không được để trống")
    @Min(value = 0, message = "Số tiền xác nhận không được âm")
    private Long expectedFinalAmount;
}

