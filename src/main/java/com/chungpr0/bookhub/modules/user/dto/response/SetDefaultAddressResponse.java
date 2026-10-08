package com.chungpr0.bookhub.modules.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class SetDefaultAddressResponse {

    @Schema(description = "ID địa chỉ", example = "12")
    private Long id;

    @JsonProperty("isDefault")
    @Schema(description = "Trạng thái mặc định", example = "true")
    private boolean isDefault;
}

