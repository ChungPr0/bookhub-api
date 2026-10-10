package com.chungpr0.bookhub.modules.user.dto.request;

import com.chungpr0.bookhub.common.enums.Role;
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
public class UpdateStaffRoleRequest {

    @NotNull(message = "Vai trò không được để trống")
    @Schema(description = "Vai trò nội bộ mới (ADMIN, MANAGER, STAFF)", example = "MANAGER")
    private Role role;
}

