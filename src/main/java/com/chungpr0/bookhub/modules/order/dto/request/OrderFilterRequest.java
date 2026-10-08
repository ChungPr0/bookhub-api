package com.chungpr0.bookhub.modules.order.dto.request;

import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest {

    @Schema(description = "Từ khóa tìm kiếm theo mã đơn hàng, tên người nhận hoặc SĐT", example = "ORD-20261007")
    private String keyword;

    @Schema(description = "Danh sách trạng thái đơn hàng cần lọc (PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED, RETURNED)")
    private List<OrderStatus> status;

    @Schema(description = "Trạng thái thanh toán (UNPAID, PAID, REFUND_PENDING, REFUNDED)", example = "UNPAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Phương thức thanh toán (COD, BANK_TRANSFER, VNPAY)", example = "VNPAY")
    private PaymentMethodCode paymentMethod;

    @Schema(description = "Lọc theo ID khách hàng", example = "1001")
    private Long customerId;

    @Schema(description = "Từ ngày đặt hàng (YYYY-MM-DD)", example = "2026-10-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdFrom;

    @Schema(description = "Đến ngày đặt hàng (YYYY-MM-DD)", example = "2026-10-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdTo;

    @Schema(description = "Giá trị đơn hàng tối thiểu (VND)", example = "100000")
    private Long finalAmountFrom;

    @Schema(description = "Giá trị đơn hàng tối đa (VND)", example = "1000000")
    private Long finalAmountTo;
}

