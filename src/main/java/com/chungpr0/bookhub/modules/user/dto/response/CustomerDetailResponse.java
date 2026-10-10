package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.common.enums.AccountStatus;
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
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponse {

    @Schema(description = "ID khách hàng", example = "1001")
    private Long id;

    @Schema(description = "ID tài khoản liên kết", example = "2001")
    private Long accountId;

    @Schema(description = "Họ và tên khách hàng", example = "Nguyễn Tiến Chung")
    private String fullName;

    @Schema(description = "Số điện thoại đăng ký", example = "0988888888")
    private String phone;

    @Schema(description = "Địa chỉ email", example = "chung@gmail.com")
    private String email;

    @Schema(description = "Giới tính", example = "MALE")
    private Gender gender;

    @Schema(description = "Ngày sinh", example = "2002-04-03")
    private LocalDate birthday;

    @Schema(description = "Đường dẫn ảnh đại diện", example = "https://cdn.bookhub.vn/avatars/user-1001.webp")
    private String avatarUrl;

    @Schema(description = "Phân hạng thành viên hiện tại", example = "SILVER")
    private CustomerTier tier;

    @Schema(description = "Số điểm thưởng khả dụng", example = "1420")
    private int rewardPoints;

    @Schema(description = "Tổng chi tiêu từ các đơn hoàn tất (VNĐ)", example = "2350000")
    private Long totalSpent;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Thời gian đăng ký tài khoản", example = "2026-03-20T10:00:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Danh sách sổ địa chỉ nhận hàng của khách")
    @Builder.Default
    private List<AddressResponse> addresses = new ArrayList<>();

    @Schema(description = "Thống kê đơn hàng của khách")
    private CustomerOrderStatsResponse orderStats;

    @Schema(description = "Tiến trình thăng hạng thành viên")
    private TierProgressResponse tierProgress;
}

