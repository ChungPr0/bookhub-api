package com.chungpr0.bookhub.modules.user.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.user.dto.request.AddressRequest;
import com.chungpr0.bookhub.modules.user.dto.response.AddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.DeleteAddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.SetDefaultAddressResponse;
import com.chungpr0.bookhub.modules.user.service.CustomerAddressService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/addresses")
@RequiredArgsConstructor
@Tag(name = "2. Customer Account & Addresses", description = "Quản lý sổ địa chỉ giao hàng của khách hàng")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;

    @Operation(
            summary = "ADR-01: Lấy danh sách địa chỉ giao hàng",
            description = "Trả về toàn bộ danh sách địa chỉ nhận hàng của khách. Địa chỉ mặc định luôn được sắp xếp lên đầu danh sách.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<AddressResponse> response = customerAddressService.getAddresses(principal.getAccountId());
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách địa chỉ thành công", response));
    }

    @Operation(
            summary = "ADR-02: Lấy chi tiết một địa chỉ",
            description = "Lấy dữ liệu chi tiết của một địa chỉ nhận hàng để hiển thị hoặc chỉnh sửa. Bảo vệ chống IDOR.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        AddressResponse response = customerAddressService.getAddressById(principal.getAccountId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết địa chỉ thành công", response));
    }

    @Operation(
            summary = "ADR-03: Thêm mới địa chỉ",
            description = "Thêm địa chỉ giao hàng mới (tối đa 10 địa chỉ). Địa chỉ đầu tiên sẽ tự động trở thành mặc định.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = customerAddressService.createAddress(principal.getAccountId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm địa chỉ giao hàng thành công", response));
    }

    @Operation(
            summary = "ADR-04: Cập nhật địa chỉ",
            description = "Cập nhật thông tin địa chỉ giao hàng đã có. Bảo vệ chống IDOR.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = customerAddressService.updateAddress(principal.getAccountId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật địa chỉ thành công", response));
    }

    @Operation(
            summary = "ADR-05: Xóa địa chỉ",
            description = "Xóa địa chỉ giao hàng. Nếu xóa địa chỉ mặc định, hệ thống tự động gán địa chỉ cập nhật gần nhất còn lại làm mặc định mới.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<DeleteAddressResponse>> deleteAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        DeleteAddressResponse response = customerAddressService.deleteAddress(principal.getAccountId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa địa chỉ thành công", response));
    }

    @Operation(
            summary = "ADR-06: Thiết lập địa chỉ mặc định nhanh",
            description = "Thiết lập địa chỉ chỉ định làm mặc định, tự động hủy cờ mặc định của các địa chỉ khác.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<SetDefaultAddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        SetDefaultAddressResponse response = customerAddressService.setDefaultAddress(principal.getAccountId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Đã thiết lập làm địa chỉ mặc định", response));
    }
}

