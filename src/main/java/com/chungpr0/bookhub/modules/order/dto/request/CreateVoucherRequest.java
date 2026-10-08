package com.chungpr0.bookhub.modules.order.dto.request;

import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class CreateVoucherRequest {

    @Schema(description = "Mã voucher viết hoa không dấu (3-30 ký tự)", example = "BOOKHUB10K")
    @NotBlank(message = "Mã voucher không được để trống")
    @Pattern(regexp = "^[A-Z0-9_]{3,30}$", message = "Mã voucher chỉ gồm chữ in hoa, số và dấu gạch dưới (3-30 ký tự)")
    private String code;

    @Schema(description = "Tên hiển thị của voucher", example = "Giảm 10K cho đơn hàng đầu tiên")
    @NotBlank(message = "Tên voucher không được để trống")
    @Size(max = 150, message = "Tên voucher không được vượt quá 150 ký tự")
    private String name;

    @Schema(description = "Mô tả chi tiết điều kiện sử dụng", example = "Áp dụng cho mọi khách hàng với đơn từ 100k")
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @Schema(description = "Loại giảm giá: PERCENTAGE hoặc FIXED_AMOUNT", example = "FIXED_AMOUNT")
    @NotNull(message = "Loại giảm giá không được để trống")
    private VoucherDiscountType discountType;

    @Schema(description = "Giá trị giảm: số % (1-100) hoặc số tiền VND", example = "10000")
    @NotNull(message = "Giá trị giảm giá không được để trống")
    @Min(value = 1, message = "Giá trị giảm giá phải lớn hơn 0")
    private Long discountValue;

    @Schema(description = "Mức giảm tối đa (dành cho loại PERCENTAGE)", example = "30000")
    @Min(value = 0, message = "Mức giảm tối đa không được âm")
    private Long maxDiscountAmount;

    @Schema(description = "Giá trị đơn hàng tối thiểu để được áp dụng", example = "100000")
    @NotNull(message = "Giá trị đơn tối thiểu không được để trống")
    @Min(value = 0, message = "Giá trị đơn tối thiểu không được âm")
    @Builder.Default
    private Long minOrderAmount = 0L;

    @Schema(description = "Tổng số lượt sử dụng toàn hệ thống (null = không giới hạn)", example = "500")
    @Min(value = 1, message = "Tổng số lượt sử dụng phải lớn hơn 0")
    private Integer usageLimit;

    @Schema(description = "Số lượt tối đa mỗi khách hàng được dùng", example = "1")
    @NotNull(message = "Số lượt dùng mỗi khách hàng không được để trống")
    @Min(value = 1, message = "Số lượt dùng mỗi khách hàng phải ít nhất là 1")
    @Builder.Default
    private Integer usageLimitPerCustomer = 1;

    @Schema(description = "Thời điểm bắt đầu áp dụng mã")
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private OffsetDateTime startDate;

    @Schema(description = "Thời điểm kết thúc hiệu lực mã")
    @NotNull(message = "Thời gian kết thúc không được để trống")
    private OffsetDateTime expirationDate;
}

