package com.chungpr0.bookhub.modules.cart.dto.response;

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
@Schema(description = "Kết quả thao tác thêm hoặc xóa sách khỏi danh sách yêu thích")
public class WishlistActionResponse {

    @Schema(description = "ID cuốn sách", example = "101")
    private Long bookId;

    @JsonProperty("isInWishlist")
    @Schema(description = "Trạng thái nằm trong danh sách yêu thích", example = "true")
    private boolean isInWishlist;
}

