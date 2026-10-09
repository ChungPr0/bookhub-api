package com.chungpr0.bookhub.modules.inventory.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateStockReceiptRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.StockReceiptFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.CreateStockReceiptResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptSummaryResponse;
import com.chungpr0.bookhub.modules.inventory.service.StockReceiptService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/stock-receipts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "7.2 Quản lý Phiếu nhập kho (Admin Stock Receipts)", description = "APIs lập phiếu nhập kho theo lô hàng loạt, xem danh sách và chi tiết phiếu nhập kho")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminStockReceiptController {

    private final StockReceiptService stockReceiptService;

    @PostMapping
    @Operation(
            summary = "STK-01: Lập phiếu nhập kho theo lô hàng loạt 🔁",
            description = "Tiếp nhận hàng loạt đầu sách từ nhà cung cấp, sinh mã phiếu PN-*, sinh lô BATCH-*, tăng tồn kho sách, ghi Thẻ kho và trả cảnh báo nghiệp vụ (nếu có)."
    )
    public ResponseEntity<ApiResponse<CreateStockReceiptResponse>> createStockReceipt(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Khóa chống trùng lặp tạo phiếu (UUID v4)")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateStockReceiptRequest request
    ) {
        CreateStockReceiptResponse response = stockReceiptService.createStockReceipt(principal.getAccountId(), idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Lập phiếu nhập kho thành công", response));
    }

    @GetMapping
    @Operation(
            summary = "STK-02: Danh sách phiếu nhập kho Admin",
            description = "Tra cứu, tìm kiếm theo mã phiếu, ghi chú, nhà cung cấp, người lập và khoảng ngày nhập."
    )
    public ResponseEntity<ApiResponse<PageResponse<StockReceiptSummaryResponse>>> getStockReceipts(
            @ModelAttribute StockReceiptFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<StockReceiptSummaryResponse> response = stockReceiptService.getStockReceipts(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu nhập kho thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "STK-03: Chi tiết phiếu nhập kho Admin",
            description = "Xem chi tiết chứng từ phiếu nhập kho cùng danh sách các lô hàng sách thành phần và đơn giá nhập."
    )
    public ResponseEntity<ApiResponse<StockReceiptDetailResponse>> getStockReceiptDetail(
            @Parameter(description = "ID phiếu nhập kho", example = "88", required = true)
            @PathVariable Long id
    ) {
        StockReceiptDetailResponse response = stockReceiptService.getStockReceiptDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phiếu nhập kho thành công", response));
    }
}

