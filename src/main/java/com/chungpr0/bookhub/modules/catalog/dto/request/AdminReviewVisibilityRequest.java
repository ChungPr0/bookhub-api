package com.chungpr0.bookhub.modules.catalog.dto.request;

import com.chungpr0.bookhub.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu kiểm duyệt hiển thị bài đánh giá (ARV-02)")
public class AdminReviewVisibilityRequest {

    @NotNull(message = "Trạng thái đánh giá không được để trống")
    @Schema(description = "Trạng thái hiển thị (VISIBLE, HIDDEN)", example = "HIDDEN")
    private ReviewStatus status;

    @Size(max = 255, message = "Lý do ẩn đánh giá tối đa 255 ký tự")
    @Schema(description = "Lý do ẩn (bắt buộc khi status = HIDDEN)", example = "Chứa từ ngữ xúc phạm, quảng cáo liên kết ngoài")
    private String reason;
}

