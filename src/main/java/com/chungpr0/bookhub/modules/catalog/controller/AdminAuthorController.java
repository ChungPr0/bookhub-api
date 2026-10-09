package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminAuthorDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminAuthorResponse;
import com.chungpr0.bookhub.modules.catalog.service.AdminAuthorService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/authors")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "6.2 Quản trị Tác giả (Admin Authors)", description = "APIs quản lý thông tin danh sách và hồ sơ tác giả dành cho Quản trị viên")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminAuthorController {

    private final AdminAuthorService adminAuthorService;

    @GetMapping
    @Operation(
            summary = "AAU-01: Danh sách tác giả Admin",
            description = "Tra cứu, tìm kiếm theo tên và phân trang danh sách tác giả kèm số lượng tác phẩm trong hệ thống."
    )
    public ResponseEntity<ApiResponse<PageResponse<AdminAuthorResponse>>> getAdminAuthors(
            @Parameter(description = "Từ khóa tìm kiếm theo tên tác giả", example = "Paulo Coelho")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang (tối đa 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tiêu chí sắp xếp (name, createdAt,desc)", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        PageResponse<AdminAuthorResponse> response = adminAuthorService.getAdminAuthors(keyword, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tác giả thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "AAU-02: Chi tiết tác giả Admin",
            description = "Xem chi tiết hồ sơ tác giả gồm tiểu sử, ảnh đại diện và số lượng tác phẩm liên kết."
    )
    public ResponseEntity<ApiResponse<AdminAuthorDetailResponse>> getAdminAuthorDetail(
            @Parameter(description = "ID tác giả", example = "7", required = true)
            @PathVariable Long id
    ) {
        AdminAuthorDetailResponse response = adminAuthorService.getAdminAuthorDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tác giả thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AAU-03: Thêm mới tác giả",
            description = "Thêm mới hồ sơ tác giả vào hệ thống (tự động sinh slug chuẩn hóa)."
    )
    public ResponseEntity<ApiResponse<AdminAuthorResponse>> createAuthor(
            @Valid @RequestBody CreateAuthorRequest request
    ) {
        AdminAuthorResponse response = adminAuthorService.createAuthor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm tác giả thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AAU-04: Cập nhật tác giả",
            description = "Cập nhật tên, tiểu sử và ảnh chân dung của tác giả."
    )
    public ResponseEntity<ApiResponse<AdminAuthorResponse>> updateAuthor(
            @Parameter(description = "ID tác giả", example = "7", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateAuthorRequest request
    ) {
        AdminAuthorResponse response = adminAuthorService.updateAuthor(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tác giả thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AAU-05: Xóa tác giả",
            description = "Xóa hồ sơ tác giả. Bị chặn nếu tác giả đang có bất kỳ cuốn sách nào liên kết trong hệ thống."
    )
    public ResponseEntity<ApiResponse<Void>> deleteAuthor(
            @Parameter(description = "ID tác giả", example = "7", required = true)
            @PathVariable Long id
    ) {
        adminAuthorService.deleteAuthor(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tác giả thành công", null));
    }
}

