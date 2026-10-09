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
@Schema(description = "Thông tin chứng từ tham chiếu của biến động kho")
public class SimpleReferenceResponse {

    @Schema(description = "Loại chứng từ tham chiếu (RECEIPT, ORDER, ADJUSTMENT)", example = "ADJUSTMENT")
    private String type;

    @Schema(description = "ID chứng từ tham chiếu", example = "701")
    private Long id;

    @Schema(description = "Mã chứng từ tham chiếu", example = "ADJ-701")
    private String code;
}

