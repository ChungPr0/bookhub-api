package com.chungpr0.bookhub.modules.catalog.dto.request;

import com.chungpr0.bookhub.common.enums.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Yêu cầu thay đổi trạng thái kinh doanh của sách (ABK-05)")
public class UpdateBookStatusRequest {

    @NotNull(message = "Trạng thái sách không được để trống")
    @Schema(description = "Trạng thái kinh doanh (ACTIVE, INACTIVE)", example = "INACTIVE")
    private BookStatus status;
}

