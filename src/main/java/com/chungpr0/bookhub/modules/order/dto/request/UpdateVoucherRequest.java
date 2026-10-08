package com.chungpr0.bookhub.modules.order.dto.request;

import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UpdateVoucherRequest {

    @Schema(description = "Tên hiển thị của voucher", example = "Giảm 10K cho đơn hàng đầu tiên (Đã gia hạn)")
    @NotBlank(message = "Tên voucher không được để trống")
    @Size(max = 150, message = "Tên voucher không được vượt quá 150 ký tự")
    private String name;

    @Schema(description = "Mô tả chi tiết điều kiện sử dụng", example = "Gia hạn thêm tới hết tháng 11")
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @Schema(description = "Loại giảm giá: PERCENTAGE hoặc FIXED_AMOUNT (Bất biến nếu usedCount > 0)", example = "FIXED_AMOUNT")
    private VoucherDiscountType discountType;

    @Schema(description = "Giá trị giảm (Bất biến nếu usedCount > 0)", example = "10000")
    @Min(value = 1, message = "Giá trị giảm giá phải lớn hơn 0")
    private Long discountValue;

    @Schema(description = "Mức giảm tối đa (Bất biến nếu usedCount > 0)", example = "30000")
    @Min(value = 0, message = "Mức giảm tối đa không được âm")
    private Long maxDiscountAmount;

    @Schema(description = "Giá trị đơn tối thiểu (Bất biến nếu usedCount > 0)", example = "100000")
    @Min(value = 0, message = "Giá trị đơn tối thiểu không được âm")
    private Long minOrderAmount;

    @Schema(description = "Tổng số lượt sử dụng toàn hệ thống (Không được nhỏ hơn usedCount hiện tại)", example = "600")
    @Min(value = 1, message = "Tổng số lượt sử dụng phải lớn hơn 0")
    private Integer usageLimit;

    @Schema(description = "Số lượt tối đa mỗi khách hàng được dùng", example = "2")
    @Min(value = 1, message = "Số lượt dùng mỗi khách hàng phải ít nhất là 1")
    private Integer usageLimitPerCustomer;

    @Schema(description = "Thời điểm bắt đầu áp dụng mã")
    private OffsetDateTime startDate;

    @Schema(description = "Thời điểm kết thúc hiệu lực mã")
    @NotNull(message = "Thời gian kết thúc không được để trống")
    private OffsetDateTime expirationDate;
}

