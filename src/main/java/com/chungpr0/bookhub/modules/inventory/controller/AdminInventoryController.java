package com.chungpr0.bookhub.modules.inventory.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.inventory.dto.request.AdjustInventoryRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.InventoryTransactionFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.LowStockFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryAdjustmentResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryTransactionPageResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.LowStockPageResponse;
import com.chungpr0.bookhub.modules.inventory.service.InventoryService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "7.4 Quản trị Kho & Thẻ kho (Admin Inventory)", description = "APIs điều chỉnh kiểm kê kho, tra cứu Thẻ kho điện tử và cảnh báo tồn kho sắp hết/hết hàng")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/adjustments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "STK-05: Điều chỉnh tồn kho (Kiểm kê, Hư hỏng, Thất lạc) 🔁",
            description = "Điều chỉnh tăng hoặc giảm tồn kho sách. Nếu giảm: tự động trừ lô theo FIFO. Nếu tăng: tạo lô điều chỉnh mới. Ghi nhật ký vào Thẻ kho."
    )
    public ResponseEntity<ApiResponse<InventoryAdjustmentResponse>> adjustInventory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Khóa chống trùng lặp điều chỉnh (UUID v4)")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody AdjustInventoryRequest request
    ) {
        InventoryAdjustmentResponse response = inventoryService.adjustInventory(principal.getAccountId(), idempotencyKey, request);
        return ResponseEntity.ok(ApiResponse.success("Điều chỉnh tồn kho thành công", response));
    }

    @GetMapping("/transactions")
    @Operation(
            summary = "STK-06: Tra cứu Thẻ kho (Inventory Transactions)",
            description = "Tra cứu nhật ký biến động xuất/nhập/điều chỉnh. Khi truyền bookId, hệ thống tự động tính toán bảng tổng hợp tồn đầu kỳ, tổng nhập, tổng xuất và tồn cuối kỳ."
    )
    public ResponseEntity<ApiResponse<InventoryTransactionPageResponse>> getTransactions(
            @ModelAttribute InventoryTransactionFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        InventoryTransactionPageResponse response = inventoryService.getTransactions(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thẻ kho thành công", response));
    }

    @GetMapping("/low-stock")
    @Operation(
            summary = "STK-07: Cảnh báo sách sắp hết hàng / Hết hàng",
            description = "Phát hiện các đầu sách có nguy cơ thiếu hàng (tồn <= ngưỡng hoặc = 0), thống kê lượng bán 30 ngày qua và ước tính số ngày còn lại."
    )
    public ResponseEntity<ApiResponse<LowStockPageResponse>> getLowStockBooks(
            @ModelAttribute LowStockFilterRequest filter,
            @PageableDefault(size = 20, sort = "stockQuantity", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        LowStockPageResponse response = inventoryService.getLowStockBooks(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cảnh báo tồn kho thành công", response));
    }
}

