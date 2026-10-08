package com.chungpr0.bookhub.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thống kê điểm đánh giá và phân bổ sao")
public class RatingSummaryResponse {

    @Schema(description = "Điểm đánh giá trung bình (1.0 - 5.0)", example = "4.7")
    private Double avgRating;

    @Schema(description = "Tổng số lượt đánh giá", example = "1284")
    private int reviewCount;

    @Schema(description = "Phân bổ số lượng đánh giá theo từng mức sao (1-5)")
    @Builder.Default
    private Map<String, Integer> distribution = new HashMap<>();
}

