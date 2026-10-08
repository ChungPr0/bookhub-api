package com.chungpr0.bookhub.modules.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableVouchersResponse {

    @Schema(description = "Danh sách mã giảm giá đủ điều kiện sử dụng ngay")
    @Builder.Default
    private List<VoucherResponse> usable = new ArrayList<>();

    @Schema(description = "Danh sách mã giảm giá chưa đủ điều kiện kèm lý do")
    @Builder.Default
    private List<UnusableVoucherResponse> unusable = new ArrayList<>();
}

