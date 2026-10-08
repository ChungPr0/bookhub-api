package com.chungpr0.bookhub.modules.catalog.dto.response;

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
@Schema(description = "Phân đoạn khoảng giá và số lượng sách tương ứng")
public class PriceRangeFacetResponse {

    @Schema(description = "Mức giá từ (VND)", example = "0")
    private Long from;

    @Schema(description = "Mức giá đến (VND, null nếu không giới hạn trên)", example = "100000")
    private Long to;

    @Schema(description = "Số lượng sách trong khoảng giá", example = "25")
    private long count;
}

