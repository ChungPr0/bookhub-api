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
@Schema(description = "Thông tin tóm tắt nhà xuất bản")
public class PublisherSummaryResponse {

    @Schema(description = "ID nhà xuất bản", example = "4")
    private Long id;

    @Schema(description = "Tên nhà xuất bản", example = "NXB Hội Nhà Văn")
    private String name;

    @Schema(description = "Slug nhà xuất bản", example = "nxb-hoi-nha-van")
    private String slug;
}

