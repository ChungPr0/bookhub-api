package com.chungpr0.bookhub.modules.user.dto.request;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UpdateStaffStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    @Schema(description = "Trạng thái tài khoản mới (ACTIVE hoặc LOCKED)", example = "LOCKED")
    private AccountStatus status;

    @Size(max = 255, message = "Lý do tối đa 255 ký tự")
    @Schema(description = "Lý do thay đổi trạng thái nhân sự", example = "Nhân viên đã nghỉ việc từ ngày 07/10/2026")
    private String reason;
}

