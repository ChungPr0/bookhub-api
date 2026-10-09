package com.chungpr0.bookhub.modules.inventory.dto.request;

import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentReason;
import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Yêu cầu điều chỉnh tồn kho (kiểm kê, hư hỏng, thất lạc)")
public class AdjustInventoryRequest {

    @NotNull(message = "ID cuốn sách không được để trống")
    @Schema(description = "ID cuốn sách cần điều chỉnh", example = "101")
    private Long bookId;

    @NotNull(message = "Loại điều chỉnh không được để trống")
    @Schema(description = "Loại điều chỉnh (ADJUST_IN: tăng kho, ADJUST_OUT: giảm kho)", example = "ADJUST_OUT")
    private AdjustmentType type;

    @NotNull(message = "Số lượng điều chỉnh không được để trống")
    @Min(value = 1, message = "Số lượng điều chỉnh tối thiểu là 1")
    @Max(value = 100000, message = "Số lượng điều chỉnh tối đa là 100.000")
    @Schema(description = "Số lượng sách cần điều chỉnh", example = "5")
    private Integer quantity;

    @NotNull(message = "Lý do điều chỉnh không được để trống")
    @Schema(description = "Lý do điều chỉnh (DAMAGED, LOST, STOCKTAKE, FOUND, OTHER)", example = "DAMAGED")
    private AdjustmentReason reason;

    @Size(max = 255, message = "Ghi chú giải trình tối đa 255 ký tự")
    @Schema(description = "Ghi chú giải trình (bắt buộc khi reason là OTHER)", example = "Sách bị dính nước mưa trong quá trình bốc dỡ tại kho")
    private String note;

    @Schema(description = "Giá vốn ước lượng của số sách tìm thấy (bắt buộc khi type là ADJUST_IN)", example = "50000")
    private Long importPrice;
}

