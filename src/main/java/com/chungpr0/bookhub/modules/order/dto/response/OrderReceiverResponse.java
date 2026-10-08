package com.chungpr0.bookhub.modules.order.dto.response;

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
public class OrderReceiverResponse {

    @Schema(description = "Họ và tên người nhận hàng", example = "Nguyễn Tiến Chung")
    private String name;

    @Schema(description = "Số điện thoại nhận hàng", example = "0988888888")
    private String phone;

    @Schema(description = "Địa chỉ nhận hàng đầy đủ (snapshot)", example = "Số 10, Ngõ 2, Trần Thái Tông, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội")
    private String address;
}

