package com.chungpr0.bookhub.modules.inventory.dto.response;

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
@Schema(description = "Chi tiết thông tin nhà cung cấp")
public class SupplierDetailResponse {

    @Schema(description = "ID nhà cung cấp", example = "1")
    private Long id;

    @Schema(description = "Tên công ty hoặc nhà phân phối", example = "Công ty Cổ phần Văn hóa & Truyền thông Nhã Nam")
    private String name;

    @Schema(description = "Họ tên người liên hệ kinh doanh", example = "Trần Văn Nam")
    private String contactName;

    @Schema(description = "Số điện thoại liên hệ", example = "02435146875")
    private String phone;

    @Schema(description = "Hòm thư liên hệ", example = "kinhdoanh@nhanam.vn")
    private String email;

    @Schema(description = "Địa chỉ trụ sở / kho", example = "59 Đỗ Quang, Trung Hòa, Cầu Giấy, Hà Nội")
    private String address;

    @Schema(description = "Mã số thuế doanh nghiệp", example = "0101824123")
    private String taxCode;

    @Schema(description = "Số lượng phiếu nhập kho đã lập", example = "18")
    private Long receiptCount;

    @Schema(description = "Tổng giá trị nhập hàng lũy kế (VND)", example = "485000000")
    private Long totalImportCost;

    @Schema(description = "Thời điểm tạo nhà cung cấp", example = "2026-01-05T09:00:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời điểm cập nhật gần nhất", example = "2026-01-05T09:00:00+07:00")
    private OffsetDateTime updatedAt;
}

