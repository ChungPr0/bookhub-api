package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.common.enums.AccountStatus;
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
public class CustomerStatusResponse {

    @Schema(description = "ID khách hàng", example = "1001")
    private Long id;

    @Schema(description = "ID tài khoản liên kết", example = "2001")
    private Long accountId;

    @Schema(description = "Trạng thái tài khoản mới", example = "LOCKED")
    private AccountStatus status;

    @Schema(description = "Lý do khóa hoặc mở khóa", example = "Phát hiện hành vi gian lận đơn hàng")
    private String reason;
}

