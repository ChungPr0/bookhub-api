package com.chungpr0.bookhub.modules.user.dto.request;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFilterRequest {

    @Schema(description = "Từ khóa tìm kiếm theo họ tên, số điện thoại hoặc email", example = "Chung")
    private String keyword;

    @Schema(description = "Lọc theo trạng thái tài khoản", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Lọc theo danh sách phân hạng thành viên", example = "[\"SILVER\", \"GOLD\"]")
    private List<CustomerTier> tier;

    @Schema(description = "Ngày đăng ký từ", example = "2026-01-01")
    private LocalDate createdFrom;

    @Schema(description = "Ngày đăng ký đến", example = "2026-12-31")
    private LocalDate createdTo;

    @Schema(description = "Tổng chi tiêu từ (VNĐ)", example = "1000000")
    private Long totalSpentFrom;

    @Schema(description = "Tổng chi tiêu đến (VNĐ)", example = "10000000")
    private Long totalSpentTo;
}

