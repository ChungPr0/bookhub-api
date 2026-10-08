package com.chungpr0.bookhub.modules.auth.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.auth.dto.request.ChangePasswordRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordOtpRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordResetRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordVerifyRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.LoginRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.LogoutRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RefreshTokenRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RegisterOtpRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RegisterRequest;
import com.chungpr0.bookhub.modules.auth.dto.response.LoginResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.OtpResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.RefreshTokenResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.RegisterResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.ResetTokenResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.UserInfoResponse;
import com.chungpr0.bookhub.modules.auth.service.AuthService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1. Authentication & Account", description = "Các API Xác thực, Đăng ký, Đăng nhập, Cấp lại Token và Quản lý mật khẩu")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * AUTH-01: Yêu cầu gửi OTP đăng ký qua SMS
     */
    @Operation(
            summary = "AUTH-01: Yêu cầu gửi OTP đăng ký",
            description = "Gửi mã xác thực OTP 6 chữ số qua SMS để xác nhận đăng ký số điện thoại. Giới hạn tối đa 5 lần/ngày và giãn cách tối thiểu 60s.",
            security = {}
    )
    @PostMapping("/register/otp")
    public ResponseEntity<ApiResponse<OtpResponse>> requestRegisterOtp(
            @Valid @RequestBody RegisterOtpRequest request) {
        OtpResponse response = authService.requestRegisterOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Mã xác thực OTP đã được gửi đến số điện thoại của bạn.", response));
    }

    /**
     * AUTH-02: Xác thực OTP & hoàn tất tạo tài khoản Khách hàng
     */
    @Operation(
            summary = "AUTH-02: Xác thực OTP & hoàn tất đăng ký",
            description = "Xác thực mã OTP và hoàn tất tạo tài khoản Khách hàng cùng một giỏ hàng trống trong một giao dịch cơ sở dữ liệu.",
            security = {}
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký tài khoản thành công! Vui lòng đăng nhập.", response));
    }

    /**
     * AUTH-03: Đăng nhập hệ thống (cấp Access & Refresh Token)
     */
    @Operation(
            summary = "AUTH-03: Đăng nhập hệ thống",
            description = "Xác thực tài khoản và mật khẩu, cấp Access Token (JWT 30 phút) và Refresh Token (7 ngày). Tự động khóa tạm thời 15 phút nếu nhập sai quá 5 lần liên tiếp.",
            security = {}
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpServletRequest);
        LoginResponse response = authService.login(request, userAgent, ipAddress);
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
    }

    /**
     * AUTH-04: Cấp mới Access Token bằng cơ chế Refresh Token Rotation
     */
    @Operation(
            summary = "AUTH-04: Cấp lại Access Token (Token Rotation)",
            description = "Sử dụng Refresh Token để cấp cặp Access Token và Refresh Token mới. Thu hồi toàn bộ token family nếu phát hiện token bị tái sử dụng trái phép.",
            security = {}
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest) {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpServletRequest);
        RefreshTokenResponse response = authService.refreshToken(request, userAgent, ipAddress);
        return ResponseEntity.ok(ApiResponse.ok("Cấp lại Token thành công", response));
    }

    /**
     * AUTH-05: Đăng xuất phiên hiện tại (thu hồi Refresh Token)
     */
    @Operation(
            summary = "AUTH-05: Đăng xuất phiên hiện tại",
            description = "Thu hồi Refresh Token của phiên đăng nhập hiện tại trên thiết bị này.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getAccountId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công", null));
    }

    /**
     * AUTH-06: Đăng xuất toàn bộ thiết bị (vô hiệu hóa tức thì toàn bộ phiên)
     */
    @Operation(
            summary = "AUTH-06: Đăng xuất toàn bộ thiết bị",
            description = "Tăng token_version của tài khoản để vô hiệu hóa tức thì toàn bộ JWT Access Token và thu hồi toàn bộ Refresh Token trên mọi thiết bị.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal UserPrincipal principal) {
        authService.logoutAll(principal.getAccountId());
        return ResponseEntity.ok(ApiResponse.ok("Đã đăng xuất khỏi toàn bộ các thiết bị", null));
    }

    /**
     * AUTH-07: Lấy thông tin tài khoản phiên hiện tại & danh sách quyền
     */
    @Operation(
            summary = "AUTH-07: Lấy thông tin tài khoản phiên hiện tại",
            description = "Lấy hồ sơ người dùng đang đăng nhập kèm danh sách phân quyền (Permissions) dựa trên JWT Access Token.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserInfoResponse response = authService.getCurrentUser(principal.getAccountId());
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin tài khoản thành công", response));
    }

    /**
     * AUTH-08: Đổi mật khẩu chủ động
     */
    @Operation(
            summary = "AUTH-08: Đổi mật khẩu chủ động",
            description = "Đổi mật khẩu người dùng, tăng token_version để đăng xuất các thiết bị khác, và cấp lại phiên đăng nhập mới cho thiết bị hiện tại.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RefreshTokenResponse response = authService.changePassword(principal.getAccountId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công. Các phiên đăng nhập trên thiết bị khác đã được đăng xuất.", response));
    }

    /**
     * AUTH-09: Quên mật khẩu - Yêu cầu gửi OTP khôi phục
     */
    @Operation(
            summary = "AUTH-09: Quên mật khẩu - Yêu cầu gửi OTP",
            description = "Gửi OTP khôi phục mật khẩu. Chống tấn công User Enumeration: luôn trả về HTTP 200 mô phỏng thành công kể cả khi số điện thoại chưa tồn tại trong hệ thống.",
            security = {}
    )
    @PostMapping("/password/forgot/otp")
    public ResponseEntity<ApiResponse<OtpResponse>> requestForgotPasswordOtp(
            @Valid @RequestBody ForgotPasswordOtpRequest request) {
        OtpResponse response = authService.requestForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Nếu số điện thoại tồn tại trên hệ thống, mã xác nhận OTP đã được gửi đến bạn.", response));
    }

    /**
     * AUTH-10: Quên mật khẩu - Xác thực OTP, nhận resetToken
     */
    @Operation(
            summary = "AUTH-10: Quên mật khẩu - Xác thực OTP",
            description = "Kiểm tra mã OTP khôi phục mật khẩu. Nếu hợp lệ, hệ thống cấp một resetToken một lần có hiệu lực trong 10 phút.",
            security = {}
    )
    @PostMapping("/password/forgot/verify")
    public ResponseEntity<ApiResponse<ResetTokenResponse>> verifyForgotPasswordOtp(
            @Valid @RequestBody ForgotPasswordVerifyRequest request) {
        ResetTokenResponse response = authService.verifyForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Xác thực mã OTP thành công", response));
    }

    /**
     * AUTH-11: Quên mật khẩu - Đặt lại mật khẩu mới bằng resetToken
     */
    @Operation(
            summary = "AUTH-11: Quên mật khẩu - Đặt lại mật khẩu mới",
            description = "Sử dụng resetToken một lần để đặt mật khẩu mới, hủy token và tăng token_version để vô hiệu hóa toàn bộ phiên cũ.",
            security = {}
    )
    @PostMapping("/password/forgot/reset")
    public ResponseEntity<ApiResponse<Void>> resetForgotPassword(
            @Valid @RequestBody ForgotPasswordResetRequest request) {
        authService.resetForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.", null));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
