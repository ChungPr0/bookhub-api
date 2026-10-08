package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.common.enums.CustomerTier;
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
public class PointsSummaryResponse {

    @Schema(description = "Số điểm thưởng khả dụng", example = "1420")
    private int rewardPoints;

    @Schema(description = "Giá trị quy đổi của 1 điểm (VNĐ)", example = "100")
    private int pointValueVnd;

    @Schema(description = "Giá trị tương đương tính theo VNĐ", example = "142000")
    private Long equivalentValueVnd;

    @Schema(description = "Hạng thành viên", example = "SILVER")
    private CustomerTier tier;

    @Schema(description = "Hệ số tích điểm theo hạng", example = "1.2")
    private double tierMultiplier;

    @Schema(description = "Tổng chi tiêu tích lũy (VNĐ)", example = "2350000")
    private Long totalSpent;

    @Schema(description = "Tiến trình thăng hạng")
    private TierProgressResponse tierProgress;
}

