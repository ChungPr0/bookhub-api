package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.modules.user.entity.Address;
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
public class AddressResponse {

    @Schema(description = "ID địa chỉ", example = "5")
    private Long id;

    @Schema(description = "Tên người nhận", example = "Nguyễn Tiến Chung")
    private String receiverName;

    @Schema(description = "Số điện thoại người nhận", example = "0988888888")
    private String receiverPhone;

    @Schema(description = "Tỉnh / Thành phố", example = "Hà Nội")
    private String province;

    @Schema(description = "Quận / Huyện", example = "Quận Cầu Giấy")
    private String district;

    @Schema(description = "Phường / Xã", example = "Phường Dịch Vọng")
    private String ward;

    @Schema(description = "Địa chỉ chi tiết", example = "Số 10, Ngõ 2, Trần Thái Tông")
    private String detailAddress;

    @Schema(description = "Địa chỉ đầy đủ ghép từ các trường", example = "Số 10, Ngõ 2, Trần Thái Tông, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội")
    private String fullAddress;

    @com.fasterxml.jackson.annotation.JsonProperty("isDefault")
    @Schema(description = "Cờ đánh dấu địa chỉ mặc định", example = "true")
    private boolean isDefault;

    @Schema(description = "Thời gian tạo", example = "2026-09-01T10:00:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất", example = "2026-09-01T10:00:00+07:00")
    private OffsetDateTime updatedAt;

    public static AddressResponse fromEntity(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .detailAddress(address.getDetailAddress())
                .fullAddress(address.getFullAddress())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
