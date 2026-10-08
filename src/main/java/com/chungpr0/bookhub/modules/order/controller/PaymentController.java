package com.chungpr0.bookhub.modules.order.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VnpayReturnResponse;
import com.chungpr0.bookhub.modules.order.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
@Tag(name = "5. Cổng thanh toán VNPAY (Payment Gateway)", description = "APIs tiếp nhận tín hiệu IPN webhook và Return URL từ cổng thanh toán trực tuyến VNPAY")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/ipn", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "PAY-02: VNPAY IPN Webhook (Server-to-Server)",
            description = "Endpoint tiếp nhận tín hiệu ngầm từ máy chủ VNPAY sau khi giao dịch hoàn tất. Kiểm tra chữ ký HMAC SHA-512, cập nhật đơn hàng và trả về envelope đặc thù của VNPAY."
    )
    public ResponseEntity<String> vnpayIpn(@RequestParam Map<String, String> allParams) {
        String result = paymentService.processVnpayIpn(allParams);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/return")
    @Operation(
            summary = "PAY-03: Tiếp nhận kết quả Return URL từ VNPAY",
            description = "Endpoint tiếp nhận điều hướng của trình duyệt khách hàng sau khi thanh toán xong trên cổng VNPAY để xác thực chữ ký và hiển thị kết quả."
    )
    public ResponseEntity<ApiResponse<VnpayReturnResponse>> vnpayReturn(@RequestParam Map<String, String> allParams) {
        VnpayReturnResponse response = paymentService.processVnpayReturn(allParams);
        return ResponseEntity.ok(ApiResponse.success("Xác thực kết quả thanh toán thành công", response));
    }
}

