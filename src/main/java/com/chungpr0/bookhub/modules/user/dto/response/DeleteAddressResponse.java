package com.chungpr0.bookhub.modules.user.dto.response;

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
public class DeleteAddressResponse {

    @Schema(description = "ID địa chỉ vừa bị xóa", example = "15")
    private Long deletedAddressId;

    @Schema(description = "ID địa chỉ được tự động chỉ định làm mặc định mới (null nếu sổ trống)", example = "5")
    private Long newDefaultAddressId;
}

