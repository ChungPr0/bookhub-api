package com.chungpr0.bookhub.modules.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class CalculateShippingRequest {

    @Schema(description = "ID địa chỉ nhận hàng từ sổ địa chỉ", example = "5")
    private Long addressId;

    @Schema(description = "Địa chỉ nhận hàng nhập trực tiếp (nếu không dùng addressId)")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @Schema(description = "Danh sách sản phẩm cần tính cước")
    @NotEmpty(message = "Danh sách sản phẩm tính cước không được để trống")
    @Valid
    private List<ShippingCalculateItemRequest> items;

    @Schema(description = "Mã voucher áp dụng (để đối chiếu ngưỡng miễn phí ship)", example = "BOOKHUB10")
    private String voucherCode;
}

