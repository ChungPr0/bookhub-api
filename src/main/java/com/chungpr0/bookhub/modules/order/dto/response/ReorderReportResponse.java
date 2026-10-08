package com.chungpr0.bookhub.modules.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderReportResponse {

    @Schema(description = "Danh sách sản phẩm được thêm thành công vào giỏ")
    @Builder.Default
    private List<ReorderItemReport> addedItems = new ArrayList<>();

    @Schema(description = "Danh sách sản phẩm bị bỏ qua do hết hàng hoặc ngừng bán")
    @Builder.Default
    private List<ReorderItemReport> skippedItems = new ArrayList<>();

    @Schema(description = "Tổng số lượng cuốn sách đã thêm vào giỏ", example = "4")
    private int totalAdded;
}

