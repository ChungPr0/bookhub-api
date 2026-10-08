package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookResponse;
import com.chungpr0.bookhub.modules.catalog.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/books")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "3.5 Quản trị Sách (Admin Books)", description = "APIs quản lý và tra cứu kỹ thuật sản phẩm sách dành cho Quản trị viên")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminBookController {

    private final BookService bookService;

    @GetMapping
    @Operation(
            summary = "ABK-01: Quản trị danh sách sách",
            description = "Tra cứu và lọc danh sách sách dành cho Admin, hiển thị số lượng tồn kho thực tế và toàn bộ trạng thái (kể cả sách đã tạm ngưng kinh doanh INACTIVE)."
    )
    public ResponseEntity<ApiResponse<PageResponse<AdminBookResponse>>> getAdminBooks(
            @Parameter(description = "Từ khóa tìm kiếm (tiêu đề, ISBN hoặc tên tác giả)", example = "Nhà Giả Kim")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái kinh doanh (ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Lọc theo ID danh mục", example = "5")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang (1 - 60)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AdminBookResponse> response = bookService.getAdminBooks(keyword, status, categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách quản trị sách thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "ABK-02: Chi tiết sách dành cho Admin",
            description = "Lấy đầy đủ thông số kỹ thuật, cấu hình tồn kho và số phiên bản (version) phục vụ quản trị và kiểm soát khóa lạc quan (Optimistic Locking)."
    )
    public ResponseEntity<ApiResponse<AdminBookDetailResponse>> getAdminBookDetail(
            @Parameter(description = "ID sách", example = "101", required = true)
            @PathVariable Long id
    ) {
        AdminBookDetailResponse response = bookService.getAdminBookDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết quản trị sách thành công", response));
    }
}

