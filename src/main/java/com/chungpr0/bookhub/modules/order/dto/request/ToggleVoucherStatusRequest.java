package com.chungpr0.bookhub.modules.order.dto.request;

import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class ToggleVoucherStatusRequest {

    @Schema(description = "Trạng thái mới của voucher: ACTIVE hoặc INACTIVE", example = "INACTIVE")
    @NotNull(message = "Trạng thái voucher không được để trống")
    private VoucherStatus status;
}

