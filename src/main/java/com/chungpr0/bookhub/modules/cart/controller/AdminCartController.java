package com.chungpr0.bookhub.modules.cart.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.cart.dto.request.CartSearchFilter;
import com.chungpr0.bookhub.modules.cart.dto.response.AdminCartResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartResponse;
import com.chungpr0.bookhub.modules.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/carts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "4. Quản lý Giỏ hàng (Admin)", description = "APIs quản trị và giám sát giỏ hàng của khách hàng")
public class AdminCartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "ADM-CRT-01: Tìm kiếm và xem danh sách giỏ hàng khách hàng",
            description = "Cho phép quản trị viên tra cứu các giỏ hàng trong hệ thống theo từ khóa tên, SĐT khách hàng và trạng thái có sản phẩm, sử dụng JPA Specification.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<AdminCartResponse>>> searchCarts(
            @ModelAttribute CartSearchFilter filter,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<AdminCartResponse> response = cartService.searchCarts(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách giỏ hàng thành công", response));
    }

    @GetMapping("/{cartId}")
    @Operation(
            summary = "ADM-CRT-02: Xem chi tiết giỏ hàng của khách hàng",
            description = "Cho phép quản trị viên xem chi tiết các sản phẩm trong một giỏ hàng cụ thể.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CartResponse>> getCartById(
            @Parameter(description = "ID giỏ hàng", example = "15") @PathVariable Long cartId
    ) {
        CartResponse response = cartService.getCartById(cartId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết giỏ hàng thành công", response));
    }
}

