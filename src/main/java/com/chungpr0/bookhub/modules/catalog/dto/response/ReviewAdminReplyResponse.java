package com.chungpr0.bookhub.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phản hồi từ quản trị viên đối với đánh giá")
public class ReviewAdminReplyResponse {

    @Schema(description = "Nội dung phản hồi", example = "Dạ cảm ơn bạn đã ủng hộ BookHub ạ!")
    private String content;

    @Schema(description = "Thời gian phản hồi", example = "2026-10-06T10:00:00+07:00")
    private OffsetDateTime repliedAt;
}

