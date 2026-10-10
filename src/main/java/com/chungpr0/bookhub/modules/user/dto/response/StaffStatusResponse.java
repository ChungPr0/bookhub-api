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
public class StaffStatusResponse {

    @Schema(description = "ID nhân viên", example = "12")
    private Long id;

    @Schema(description = "ID tài khoản", example = "2012")
    private Long accountId;

    @Schema(description = "Trạng thái tài khoản mới", example = "LOCKED")
    private AccountStatus status;

    @Schema(description = "Lý do thay đổi trạng thái", example = "Nhân viên đã nghỉ việc từ ngày 07/10/2026")
    private String reason;
}

