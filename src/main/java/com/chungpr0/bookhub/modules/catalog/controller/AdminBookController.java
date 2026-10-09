package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateBookRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateBookRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateBookStatusRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookResponse;
import com.chungpr0.bookhub.modules.catalog.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/books")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ABK-03: Thêm mới sách",
            description = "Tạo mới sản phẩm sách vào danh mục quản trị. Tồn kho ban đầu luôn bằng 0 và tăng thông qua phiếu nhập kho."
    )
    public ResponseEntity<ApiResponse<AdminBookDetailResponse>> createBook(
            @Valid @RequestBody CreateBookRequest request
    ) {
        AdminBookDetailResponse response = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo mới sản phẩm sách thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ABK-04: Cập nhật sách (Optimistic Locking)",
            description = "Cập nhật thông số kỹ thuật, hình ảnh và tác giả cuốn sách. Yêu cầu gửi kèm version để kiểm soát xung đột dữ liệu."
    )
    public ResponseEntity<ApiResponse<AdminBookDetailResponse>> updateBook(
            @Parameter(description = "ID sách", example = "101", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request
    ) {
        AdminBookDetailResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin sách thành công", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ABK-05: Mở bán / Tạm ngưng kinh doanh",
            description = "Thay đổi trạng thái hiển thị của cuốn sách giữa ACTIVE và INACTIVE một cách tường minh."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateBookStatus(
            @Parameter(description = "ID sách", example = "101", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookStatusRequest request
    ) {
        bookService.updateBookStatus(id, request.getStatus());
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("status", request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái kinh doanh thành công", data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ABK-06: Xóa sách",
            description = "Xóa hoàn toàn sản phẩm sách khỏi hệ thống nếu sách chưa từng phát sinh đơn hàng hoặc giao dịch nhập kho."
    )
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @Parameter(description = "ID sách", example = "101", required = true)
            @PathVariable Long id
    ) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm sách thành công", null));
    }
}
