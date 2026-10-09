package com.chungpr0.bookhub.modules.inventory.dto.response;

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
@Schema(description = "Thông tin tóm tắt nhà cung cấp")
public class SimpleSupplierResponse {

    @Schema(description = "ID nhà cung cấp", example = "1")
    private Long id;

    @Schema(description = "Tên nhà cung cấp", example = "Công ty Cổ phần Văn hóa & Truyền thông Nhã Nam")
    private String name;
}

