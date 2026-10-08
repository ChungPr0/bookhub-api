package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.common.dto.PageResponse;
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
public class PointsOverviewResponse {

    @Schema(description = "Thông tin tổng quan điểm thưởng và hạng thành viên")
    private PointsSummaryResponse summary;

    @Schema(description = "Lịch sử biến động điểm kèm phân trang")
    private PageResponse<PointTransactionResponse> history;
}

