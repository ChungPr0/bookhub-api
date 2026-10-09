package com.chungpr0.bookhub.modules.catalog.dto.response;

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
@Schema(description = "Chi tiết thông tin nhà xuất bản dành cho Quản trị viên (APB-02)")
public class AdminPublisherDetailResponse {

    @Schema(description = "ID nhà xuất bản", example = "3")
    private Long id;

    @Schema(description = "Tên nhà xuất bản", example = "NXB Trẻ")
    private String name;

    @Schema(description = "Đường dẫn thân thiện (slug)", example = "nxb-tre")
    private String slug;

    @Schema(description = "Địa chỉ trụ sở", example = "161B Lý Chính Thắng, P. Võ Thị Sáu, Q.3, TP.HCM")
    private String address;

    @Schema(description = "Trang web chính thức", example = "https://www.nxbtre.com.vn")
    private String website;

    @Schema(description = "Số lượng tác phẩm trong hệ thống", example = "156")
    private long bookCount;

    @Schema(description = "Thời gian tạo", example = "2026-01-10T08:00:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-10-07T17:45:56+07:00")
    private OffsetDateTime updatedAt;
}

