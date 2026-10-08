package com.chungpr0.bookhub.modules.cart.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.cart.dto.request.AddToCartRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartMergeRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.UpdateCartItemQuantityRequest;
import com.chungpr0.bookhub.modules.cart.dto.response.CartMergeResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartResponse;
import com.chungpr0.bookhub.modules.cart.service.CartService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "4. Giỏ hàng (Shopping Cart)", description = "APIs quản lý giỏ hàng trực tuyến cho khách hàng")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "CRT-01: Lấy thông tin chi tiết giỏ hàng hiện tại",
            description = "Trả về toàn bộ giỏ hàng của khách hàng cùng tình trạng tồn kho thời gian thực, tổng tiền và số tiền có thể thanh toán (selectableSubtotal).",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CartResponse response = cartService.getCart(principal.getAccountId());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin giỏ hàng thành công", response));
    }

    @PostMapping("/items")
    @Operation(
            summary = "CRT-02: Thêm sách vào giỏ hàng",
            description = "Thêm sách vào giỏ hàng. Nếu sách đã có trong giỏ, tự động cộng dồn số lượng. Kiểm tra tồn kho khả dụng và giới hạn 50 đầu sách.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToCartRequest request
    ) {
        CartResponse response = cartService.addToCart(principal.getAccountId(), request);
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm vào giỏ hàng thành công", response));
    }

    @PatchMapping("/items/{bookId}")
    @Operation(
            summary = "CRT-03: Cập nhật số lượng chốt cuối của một cuốn sách",
            description = "Cập nhật số lượng chốt cuối cùng (1 - 99) cho một cuốn sách trong giỏ hàng. Kiểm tra tồn kho thời gian thực.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID của cuốn sách", example = "101") @PathVariable Long bookId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        CartResponse response = cartService.updateItemQuantity(principal.getAccountId(), bookId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật số lượng sản phẩm thành công", response));
    }

    @DeleteMapping("/items/{bookId}")
    @Operation(
            summary = "CRT-04: Xóa một cuốn sách khỏi giỏ hàng",
            description = "Xóa hoàn toàn một đầu sách khỏi giỏ hàng. Trả về cấu trúc giỏ hàng mới nhất.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID của cuốn sách", example = "101") @PathVariable Long bookId
    ) {
        CartResponse response = cartService.removeItem(principal.getAccountId(), bookId);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng thành công", response));
    }

    @DeleteMapping("/items")
    @Operation(
            summary = "CRT-05: Xóa nhiều cuốn sách được chọn khỏi giỏ hàng",
            description = "Xóa danh sách các đầu sách được chọn theo danh sách bookIds truyền qua query param. Bỏ qua các ID không có trong giỏ.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CartResponse>> removeItems(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Danh sách ID sách cần xóa (phân tách bởi dấu phẩy)", example = "101,105")
            @RequestParam(name = "bookIds", required = false) List<Long> bookIds
    ) {
        CartResponse response = cartService.removeItems(principal.getAccountId(), bookIds);
        return ResponseEntity.ok(ApiResponse.success("Xóa các sản phẩm đã chọn thành công", response));
    }

    @DeleteMapping
    @Operation(
            summary = "CRT-06: Xóa sạch toàn bộ giỏ hàng",
            description = "Xóa sạch tất cả các món đồ trong giỏ hàng hiện tại của khách hàng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CartResponse response = cartService.clearCart(principal.getAccountId());
        return ResponseEntity.ok(ApiResponse.success("Đã làm trống giỏ hàng thành công", response));
    }

    @PostMapping("/merge")
    @Operation(
            summary = "CRT-07: Gộp giỏ hàng vãng lai từ LocalStorage vào tài khoản",
            description = "Gộp các cuốn sách từ giỏ hàng lưu tạm ở LocalStorage vào tài khoản sau đăng nhập với cơ chế Smart Merge tự động điều chỉnh tồn kho.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CartMergeResponse>> mergeCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CartMergeRequest request
    ) {
        CartMergeResponse response = cartService.mergeCart(principal.getAccountId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã đồng bộ giỏ hàng thành công", response));
    }
}

