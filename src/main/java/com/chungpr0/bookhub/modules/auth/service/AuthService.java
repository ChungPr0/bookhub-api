package com.chungpr0.bookhub.modules.auth.service;

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

public interface AuthService {

    OtpResponse requestRegisterOtp(RegisterOtpRequest request);

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String userAgent, String ipAddress);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress);

    void logout(Long accountId, LogoutRequest request);

    void logoutAll(Long accountId);

    UserInfoResponse getCurrentUser(Long accountId);

    RefreshTokenResponse changePassword(Long accountId, ChangePasswordRequest request);

    OtpResponse requestForgotPasswordOtp(ForgotPasswordOtpRequest request);

    ResetTokenResponse verifyForgotPasswordOtp(ForgotPasswordVerifyRequest request);

    void resetForgotPassword(ForgotPasswordResetRequest request);
}

