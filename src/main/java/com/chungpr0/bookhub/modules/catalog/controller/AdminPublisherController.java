package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminPublisherDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminPublisherResponse;
import com.chungpr0.bookhub.modules.catalog.service.AdminPublisherService;
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
@RequestMapping("/api/v1/admin/publishers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "6.3 Quản trị Nhà xuất bản (Admin Publishers)", description = "APIs quản lý danh bạ và chi tiết nhà xuất bản dành cho Quản trị viên")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminPublisherController {

    private final AdminPublisherService adminPublisherService;

    @GetMapping
    @Operation(
            summary = "APB-01: Danh sách nhà xuất bản Admin",
            description = "Tra cứu, tìm kiếm theo tên và phân trang danh sách nhà xuất bản kèm số lượng ấn phẩm."
    )
    public ResponseEntity<ApiResponse<PageResponse<AdminPublisherResponse>>> getAdminPublishers(
            @Parameter(description = "Từ khóa tìm kiếm theo tên nhà xuất bản", example = "NXB Trẻ")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang (tối đa 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tiêu chí sắp xếp (name, createdAt,desc)", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        PageResponse<AdminPublisherResponse> response = adminPublisherService.getAdminPublishers(keyword, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà xuất bản thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "APB-02: Chi tiết nhà xuất bản Admin",
            description = "Lấy đầy đủ thông tin giới thiệu, địa chỉ trụ sở, website và số lượng đầu sách của một nhà xuất bản."
    )
    public ResponseEntity<ApiResponse<AdminPublisherDetailResponse>> getAdminPublisherDetail(
            @Parameter(description = "ID nhà xuất bản", example = "3", required = true)
            @PathVariable Long id
    ) {
        AdminPublisherDetailResponse response = adminPublisherService.getAdminPublisherDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhà xuất bản thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "APB-03: Thêm mới nhà xuất bản",
            description = "Thêm mới hồ sơ nhà xuất bản vào hệ thống (chặn nếu trùng tên nhà xuất bản)."
    )
    public ResponseEntity<ApiResponse<AdminPublisherResponse>> createPublisher(
            @Valid @RequestBody CreatePublisherRequest request
    ) {
        AdminPublisherResponse response = adminPublisherService.createPublisher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm nhà xuất bản thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "APB-04: Cập nhật nhà xuất bản",
            description = "Cập nhật tên, địa chỉ và trang web của nhà xuất bản."
    )
    public ResponseEntity<ApiResponse<AdminPublisherResponse>> updatePublisher(
            @Parameter(description = "ID nhà xuất bản", example = "3", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdatePublisherRequest request
    ) {
        AdminPublisherResponse response = adminPublisherService.updatePublisher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhà xuất bản thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "APB-05: Xóa nhà xuất bản",
            description = "Xóa hồ sơ nhà xuất bản. Bị chặn nếu nhà xuất bản đang liên kết với bất kỳ cuốn sách nào."
    )
    public ResponseEntity<ApiResponse<Void>> deletePublisher(
            @Parameter(description = "ID nhà xuất bản", example = "3", required = true)
            @PathVariable Long id
    ) {
        adminPublisherService.deletePublisher(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhà xuất bản thành công", null));
    }
}

