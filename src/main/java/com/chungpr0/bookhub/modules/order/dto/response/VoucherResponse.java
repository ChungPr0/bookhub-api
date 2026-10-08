package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherState;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
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
public class VoucherResponse {

    @Schema(description = "ID voucher", example = "5")
    private Long id;

    @Schema(description = "Mã voucher", example = "BOOKHUB10")
    private String code;

    @Schema(description = "Tên hiển thị voucher", example = "Giảm 10% đơn từ 200k")
    private String name;

    @Schema(description = "Mô tả điều kiện áp dụng", example = "Giảm tối đa 30.000đ cho đơn hàng từ 200.000đ")
    private String description;

    @Schema(description = "Loại giảm giá (PERCENTAGE, FIXED_AMOUNT)", example = "PERCENTAGE")
    private VoucherDiscountType discountType;

    @Schema(description = "Giá trị giảm (% hoặc VND)", example = "10")
    private Long discountValue;

    @Schema(description = "Mức giảm tối đa (VND)", example = "30000")
    private Long maxDiscountAmount;

    @Schema(description = "Giá trị đơn hàng tối thiểu (VND)", example = "200000")
    private Long minOrderAmount;

    @Schema(description = "Giới hạn tổng lượt sử dụng", example = "500")
    private Integer usageLimit;

    @Schema(description = "Giới hạn lượt dùng mỗi khách hàng", example = "1")
    private Integer usageLimitPerCustomer;

    @Schema(description = "Số lượt đã sử dụng thực tế", example = "142")
    private Integer usedCount;

    @Schema(description = "Thời điểm bắt đầu hiệu lực")
    private OffsetDateTime startDate;

    @Schema(description = "Thời điểm hết hạn")
    private OffsetDateTime expirationDate;

    @Schema(description = "Trạng thái cấu hình lưu trong CSDL", example = "ACTIVE")
    private VoucherStatus status;

    @Schema(description = "Trạng thái thực tế thời gian thực (UPCOMING, ACTIVE, EXPIRED, EXHAUSTED, DISABLED)", example = "ACTIVE")
    private VoucherState state;

    @Schema(description = "Phiên bản dữ liệu (Optimistic Lock)", example = "1")
    private int version;

    @Schema(description = "Thời gian tạo bản ghi")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật bản ghi gần nhất")
    private OffsetDateTime updatedAt;

    @Schema(description = "Số tiền giảm giá ước tính dựa trên subtotal (nếu có)", example = "25000")
    private Long estimatedDiscount;
}

