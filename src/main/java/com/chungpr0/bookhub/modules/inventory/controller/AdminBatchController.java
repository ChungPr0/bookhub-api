package com.chungpr0.bookhub.modules.inventory.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.inventory.dto.request.BatchFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.BatchResponse;
import com.chungpr0.bookhub.modules.inventory.service.StockReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/batches")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "7.3 Quản lý Lô hàng (Admin Batches)", description = "APIs theo dõi danh sách các lô hàng đang lưu kho theo cơ chế nhập trước xuất trước FIFO")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminBatchController {

    private final StockReceiptService stockReceiptService;

    @GetMapping
    @Operation(
            summary = "STK-04: Danh sách các lô hàng đang lưu kho (FIFO)",
            description = "Tra cứu danh sách các lô hàng, số lượng nhập ban đầu và tồn kho thực tế của lô. Mặc định sắp xếp ngày nhập sớm nhất lên đầu (FIFO: importDate,asc)."
    )
    public ResponseEntity<ApiResponse<PageResponse<BatchResponse>>> getBatches(
            @ModelAttribute BatchFilterRequest filter,
            @PageableDefault(size = 20, sort = "importDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<BatchResponse> response = stockReceiptService.getBatches(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lô hàng thành công", response));
    }
}

