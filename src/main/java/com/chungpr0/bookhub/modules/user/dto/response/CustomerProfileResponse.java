package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {

    @Schema(description = "ID khách hàng", example = "1001")
    private Long id;

    @Schema(description = "Họ và tên khách hàng", example = "Nguyễn Tiến Chung")
    private String fullName;

    @Schema(description = "Số điện thoại tài khoản", example = "0988888888")
    private String phone;

    @Schema(description = "Địa chỉ email", example = "chung@gmail.com")
    private String email;

    @Schema(description = "Giới tính", example = "MALE")
    private Gender gender;

    @Schema(description = "Ngày sinh", example = "2002-04-03")
    private LocalDate birthday;

    @Schema(description = "Đường dẫn ảnh đại diện", example = "https://cdn.bookhub.vn/avatars/user-1001.webp")
    private String avatarUrl;

    @Schema(description = "Hạng thành viên", example = "BRONZE")
    private CustomerTier tier;

    @Schema(description = "Điểm thưởng tích lũy khả dụng", example = "1420")
    private int rewardPoints;

    @Schema(description = "Tổng chi tiêu tích lũy (VNĐ)", example = "1450000")
    private Long totalSpent;

    @Schema(description = "Tiến trình thăng hạng")
    private TierProgressResponse tierProgress;

    @Schema(description = "Thời gian tạo tài khoản", example = "2026-03-20T10:00:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất", example = "2026-03-20T10:00:00+07:00")
    private OffsetDateTime updatedAt;
}

