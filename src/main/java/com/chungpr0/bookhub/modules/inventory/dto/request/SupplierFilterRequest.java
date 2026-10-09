package com.chungpr0.bookhub.modules.inventory.dto.request;

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
@Schema(description = "Tham số lọc danh sách nhà cung cấp")
public class SupplierFilterRequest {

    @Schema(description = "Từ khóa tìm kiếm theo tên NCC, MST hoặc SĐT", example = "Nhã Nam")
    private String keyword;
}

