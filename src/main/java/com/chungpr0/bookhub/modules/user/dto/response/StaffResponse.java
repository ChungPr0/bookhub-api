package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {

    @Schema(description = "ID nhân viên", example = "12")
    private Long id;

    @Schema(description = "ID tài khoản liên kết", example = "2012")
    private Long accountId;

    @Schema(description = "Họ và tên nhân viên", example = "Trần Thị Thu")
    private String fullName;

    @Schema(description = "Số điện thoại tài khoản", example = "0977112233")
    private String phone;

    @Schema(description = "Địa chỉ email nhân viên", example = "thuthu@bookhub.vn")
    private String email;

    @Schema(description = "Đường dẫn ảnh đại diện", example = "https://cdn.bookhub.vn/avatars/staff-12.webp")
    private String avatarUrl;

    @Schema(description = "Vai trò nội bộ", example = "STAFF")
    private Role role;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Thời điểm đăng nhập gần nhất", example = "2026-10-07T08:30:00+07:00")
    private OffsetDateTime lastLoginAt;

    @Schema(description = "ID tài khoản người tạo nhân viên này", example = "1")
    private Long createdBy;

    @Schema(description = "Thời gian tạo tài khoản", example = "2026-02-15T09:00:00+07:00")
    private OffsetDateTime createdAt;

    public static StaffResponse fromEntity(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .accountId(staff.getAccount() != null ? staff.getAccount().getId() : null)
                .fullName(staff.getFullName())
                .phone(staff.getAccount() != null ? staff.getAccount().getUsername() : null)
                .email(staff.getEmail())
                .avatarUrl(staff.getAvatarUrl())
                .role(staff.getAccount() != null ? staff.getAccount().getRole() : null)
                .status(staff.getAccount() != null ? staff.getAccount().getStatus() : null)
                .lastLoginAt(staff.getAccount() != null ? staff.getAccount().getLastLoginAt() : null)
                .createdBy(staff.getCreatedBy())
                .createdAt(staff.getCreatedAt())
                .build();
    }
}

