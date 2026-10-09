package com.chungpr0.bookhub.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Yêu cầu gửi bài đánh giá mới cho sản phẩm sách (REV-03)")
public class CreateReviewRequest {

    @NotBlank(message = "Mã đơn hàng không được để trống")
    @Schema(description = "Mã đơn hàng đã mua cuốn sách", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @NotNull(message = "ID cuốn sách không được để trống")
    @Schema(description = "ID cuốn sách cần đánh giá", example = "101")
    private Long bookId;

    @NotNull(message = "Số sao đánh giá không được để trống")
    @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Số sao đánh giá tối đa là 5")
    @Schema(description = "Số sao đánh giá (1 đến 5)", example = "5")
    private Integer rating;

    @Size(min = 10, max = 2000, message = "Nội dung nhận xét phải từ 10 đến 2,000 ký tự")
    @Schema(description = "Nhận xét chi tiết (tùy chọn, nếu nhập phải từ 10 - 2,000 ký tự)", example = "Sách bọc màng co cẩn thận, bìa cứng cáp, giấy in ngà vàng chống lóa mắt rất thích.")
    private String content;
}

