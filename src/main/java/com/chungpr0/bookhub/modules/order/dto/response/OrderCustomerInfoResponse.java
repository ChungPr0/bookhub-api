package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.common.enums.CustomerTier;
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
public class OrderCustomerInfoResponse {

    @Schema(description = "ID khách hàng", example = "1001")
    private Long id;

    @Schema(description = "Họ và tên khách hàng", example = "Nguyễn Tiến Chung")
    private String fullName;

    @Schema(description = "Số điện thoại tài khoản", example = "0988888888")
    private String phone;

    @Schema(description = "Địa chỉ email", example = "chung@gmail.com")
    private String email;

    @Schema(description = "Hạng thành viên (BRONZE, SILVER, GOLD)", example = "SILVER")
    private CustomerTier tier;
}

