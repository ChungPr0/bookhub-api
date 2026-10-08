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
public class TierProgressResponse {

    @Schema(description = "Hạng thành viên hiện tại", example = "BRONZE")
    private CustomerTier currentTier;

    @Schema(description = "Hạng thành viên tiếp theo (null nếu đã đạt hạng tối đa)", example = "SILVER")
    private CustomerTier nextTier;

    @Schema(description = "Mức chi tiêu tối thiểu để đạt hạng tiếp theo", example = "2000000")
    private Long nextTierThreshold;

    @Schema(description = "Số tiền còn thiếu để thăng hạng (VNĐ)", example = "550000")
    private Long amountToNextTier;

    @Schema(description = "Phần trăm tiến trình thăng hạng (0-100)", example = "72")
    private int progressPercent;
}

