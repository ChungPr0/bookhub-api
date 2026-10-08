package com.chungpr0.bookhub.modules.order.dto.response;

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
public class ShippingMilestoneResponse {

    @Schema(description = "Mã trạng thái bưu kiện (ORDER_CREATED, PACKAGE_PACKED, HANDED_OVER, IN_TRANSIT, DELIVERED)", example = "ORDER_CREATED")
    private String status;

    @Schema(description = "Tiêu đề mốc hành trình", example = "Đơn hàng đã được khởi tạo")
    private String title;

    @Schema(description = "Mô tả chi tiết sự kiện vận chuyển", example = "Hệ thống ghi nhận đơn hàng thành công")
    private String description;

    @Schema(description = "Địa điểm ghi nhận mốc", example = "Tổng kho BookHub - TP.HCM")
    private String location;

    @Schema(description = "Thời gian ghi nhận mốc hành trình")
    private OffsetDateTime timestamp;
}

