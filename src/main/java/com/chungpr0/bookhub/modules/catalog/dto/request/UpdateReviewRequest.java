package com.chungpr0.bookhub.modules.catalog.dto.request;

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
@Schema(description = "Yêu cầu chỉnh sửa bài đánh giá (REV-04)")
public class UpdateReviewRequest {

    @NotNull(message = "Số sao đánh giá không được để trống")
    @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Số sao đánh giá tối đa là 5")
    @Schema(description = "Số sao đánh giá (1 đến 5)", example = "4")
    private Integer rating;

    @Size(min = 10, max = 2000, message = "Nội dung nhận xét phải từ 10 đến 2,000 ký tự")
    @Schema(description = "Nội dung nhận xét chi tiết sau khi cập nhật", example = "Nội dung sách rất hay nhưng góc bìa bị cấn nhẹ một chút trong quá trình vận chuyển.")
    private String content;
}

