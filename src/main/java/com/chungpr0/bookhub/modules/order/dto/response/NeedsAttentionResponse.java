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
public class NeedsAttentionResponse {

    @Schema(description = "Số đơn chuyển khoản ngân hàng đang chờ duyệt tiền", example = "4")
    private long unpaidBankTransfer;

    @Schema(description = "Số đơn đang chờ hoàn tiền cho khách", example = "2")
    private long refundPending;
}

