package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
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
public class PaymentMethodResponse {

    @Schema(description = "ID phương thức thanh toán", example = "1")
    private Long id;

    @Schema(description = "Mã phương thức (COD, BANK_TRANSFER, VNPAY)", example = "COD")
    private PaymentMethodCode code;

    @Schema(description = "Tên phương thức thanh toán", example = "Thanh toán khi nhận hàng (COD)")
    private String name;

    @Schema(description = "Mô tả phương thức", example = "Thanh toán bằng tiền mặt khi shipper giao sách tới nhà")
    private String description;

    @Schema(description = "Trạng thái kích hoạt", example = "true")
    private boolean isActive;

    @Schema(description = "Thứ tự sắp xếp hiển thị", example = "1")
    private int sortOrder;

    @Schema(description = "Thông tin tài khoản ngân hàng (nếu là phương thức BANK_TRANSFER)")
    private BankInfoResponse bankInfo;
}

