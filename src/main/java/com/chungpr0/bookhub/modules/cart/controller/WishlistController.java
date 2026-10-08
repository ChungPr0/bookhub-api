package com.chungpr0.bookhub.modules.cart.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.cart.dto.request.WishlistFilter;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistActionResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistItemResponse;
import com.chungpr0.bookhub.modules.cart.service.WishlistService;
import com.chungpr0.bookhub.security.UserPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "4. Danh sách Yêu thích (Wishlist)", description = "APIs quản lý danh sách yêu thích cho khách hàng")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(
            summary = "WSH-01: Lấy danh sách sách yêu thích có phân trang",
            description = "Lấy danh sách các cuốn sách nằm trong danh sách yêu thích của khách hàng, hỗ trợ tìm kiếm động và phân trang.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<WishlistItemResponse>>> getWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @ModelAttribute WishlistFilter filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<WishlistItemResponse> response = wishlistService.getWishlist(principal.getAccountId(), filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu thích thành công", response));
    }

    @PutMapping("/{bookId}")
    @Operation(
            summary = "WSH-02: Thêm một cuốn sách vào danh sách yêu thích",
            description = "Thêm sách vào danh sách yêu thích. Thao tác mang tính Idempotent. Giới hạn tối đa 200 cuốn sách.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<WishlistActionResponse>> addToWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID của cuốn sách", example = "101") @PathVariable Long bookId
    ) {
        WishlistActionResponse response = wishlistService.addToWishlist(principal.getAccountId(), bookId);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm cuốn sách vào danh sách yêu thích", response));
    }

    @DeleteMapping("/{bookId}")
    @Operation(
            summary = "WSH-03: Xóa cuốn sách khỏi danh sách yêu thích",
            description = "Xóa sách khỏi danh sách yêu thích. Thao tác mang tính Idempotent.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<WishlistActionResponse>> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID của cuốn sách", example = "101") @PathVariable Long bookId
    ) {
        WishlistActionResponse response = wishlistService.removeFromWishlist(principal.getAccountId(), bookId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa cuốn sách khỏi danh sách yêu thích", response));
    }
}

