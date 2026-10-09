package com.chungpr0.bookhub.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Yêu cầu phản hồi đánh giá của khách hàng từ phía cửa hàng (ARV-03)")
public class AdminReviewReplyRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    @Size(min = 2, max = 1000, message = "Nội dung phản hồi phải từ 2 đến 1,000 ký tự")
    @Schema(description = "Nội dung phản hồi chính thức từ cửa hàng", example = "Dạ BookHub thành thật xin lỗi bạn vì sự cố cấn góc bìa do vận chuyển. Bạn vui lòng nhắn tin cho shop để được đổi cuốn mới nguyên vẹn nhé ạ!")
    private String content;
}

