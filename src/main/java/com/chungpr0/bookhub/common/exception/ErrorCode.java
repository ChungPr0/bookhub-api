package com.chungpr0.bookhub.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // COMMON
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Dữ liệu không hợp lệ, vui lòng kiểm tra lại"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Yêu cầu không hợp lệ hoặc dữ liệu sai định dạng JSON"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "Tham số không hợp lệ"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Vui lòng đăng nhập để tiếp tục"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Phiên làm việc đã hết hạn"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "Token không hợp lệ"),
    TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "TOKEN_REVOKED", "Phiên đăng nhập đã bị hủy"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Bạn không có quyền thực hiện thao tác này"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED", "Tài khoản của bạn đã bị khóa bởi quản trị viên"),
    PASSWORD_CHANGE_REQUIRED(HttpStatus.FORBIDDEN, "PASSWORD_CHANGE_REQUIRED", "Tài khoản cần đổi mật khẩu để tiếp tục"),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND", "Đường dẫn không tồn tại"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Không tìm thấy tài nguyên"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Phương thức HTTP không được hỗ trợ"),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION", "Bản ghi đã bị người khác sửa"),
    IDEMPOTENCY_KEY_REUSED(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", "Trùng khóa Idempotency với nội dung khác"),
    REQUEST_IN_PROGRESS(HttpStatus.CONFLICT, "REQUEST_IN_PROGRESS", "Yêu cầu cùng Idempotency-Key đang được xử lý"),
    PAYLOAD_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "PAYLOAD_TOO_LARGE", "Dung lượng dữ liệu vượt quá giới hạn"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Định dạng dữ liệu không được hỗ trợ"),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.PRECONDITION_REQUIRED, "IDEMPOTENCY_KEY_REQUIRED", "Thiếu header Idempotency-Key"),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Quá nhiều yêu cầu, vui lòng thử lại sau"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Có lỗi xảy ra, vui lòng thử lại sau"),
    UPSTREAM_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "UPSTREAM_SERVICE_ERROR", "Dịch vụ liên kết tạm thời gặp sự cố"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "Hệ thống đang bảo trì, vui lòng thử lại sau"),
    UPSTREAM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "UPSTREAM_TIMEOUT", "Dịch vụ liên kết không phản hồi kịp thời"),

    // AUTH
    PHONE_ALREADY_REGISTERED(HttpStatus.CONFLICT, "PHONE_ALREADY_REGISTERED", "Số điện thoại này đã được đăng ký, vui lòng đăng nhập"),
    EMAIL_ALREADY_IN_USE(HttpStatus.CONFLICT, "EMAIL_ALREADY_IN_USE", "Địa chỉ email đã được sử dụng bởi một tài khoản khác"),
    OTP_REQUEST_TOO_FREQUENT(HttpStatus.TOO_MANY_REQUESTS, "OTP_REQUEST_TOO_FREQUENT", "Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới"),
    OTP_DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "OTP_DAILY_LIMIT_EXCEEDED", "Bạn đã vượt quá số lần nhận mã OTP cho phép trong ngày"),
    OTP_INVALID(HttpStatus.UNPROCESSABLE_CONTENT, "OTP_INVALID", "Mã xác thực OTP không chính xác"),
    OTP_EXPIRED(HttpStatus.GONE, "OTP_EXPIRED", "Mã OTP đã hết hiệu lực, vui lòng yêu cầu mã mới"),
    OTP_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "OTP_ATTEMPTS_EXCEEDED", "Bạn đã nhập sai mã OTP quá 5 lần. Mã đã bị hủy"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Số điện thoại hoặc mật khẩu không chính xác"),
    ACCOUNT_TEMPORARILY_LOCKED(HttpStatus.LOCKED, "ACCOUNT_TEMPORARILY_LOCKED", "Tài khoản tạm thời bị khóa do nhập sai mật khẩu nhiều lần. Vui lòng thử lại sau 15 phút"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "Phiên đăng nhập không hợp lệ"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REUSED", "Phát hiện phiên truy cập đáng ngờ. Toàn bộ phiên đã bị hủy vì lý do bảo mật"),
    CURRENT_PASSWORD_INCORRECT(HttpStatus.UNPROCESSABLE_CONTENT, "CURRENT_PASSWORD_INCORRECT", "Mật khẩu hiện tại không chính xác"),
    PASSWORD_REUSED(HttpStatus.UNPROCESSABLE_CONTENT, "PASSWORD_REUSED", "Mật khẩu mới không được trùng với mật khẩu hiện tại"),
    RESET_TOKEN_INVALID(HttpStatus.UNPROCESSABLE_CONTENT, "RESET_TOKEN_INVALID", "Mã xác nhận đặt lại mật khẩu không hợp lệ"),
    RESET_TOKEN_EXPIRED(HttpStatus.GONE, "RESET_TOKEN_EXPIRED", "Phiên đặt lại mật khẩu đã hết hạn, vui lòng thao tác lại từ đầu"),
    SMS_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "SMS_PROVIDER_ERROR", "Hệ thống gửi tin nhắn tạm thời gián đoạn, vui lòng thử lại sau"),

    // MODULE 02 - CUSTOMER & ADDRESS
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ giao hàng"),
    ADDRESS_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_CONTENT, "ADDRESS_LIMIT_EXCEEDED", "Bạn chỉ có thể lưu tối đa 10 địa chỉ giao hàng"),

    // MODULE 03 - CATALOG
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "Không tìm thấy danh mục yêu cầu"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND", "Không tìm thấy cuốn sách yêu cầu"),
    AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTHOR_NOT_FOUND", "Không tìm thấy tác giả yêu cầu"),
    PUBLISHER_NOT_FOUND(HttpStatus.NOT_FOUND, "PUBLISHER_NOT_FOUND", "Không tìm thấy nhà xuất bản yêu cầu"),

    // MODULE 04 - CART & WISHLIST
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "Sản phẩm không có trong giỏ hàng"),
    BOOK_NOT_AVAILABLE(HttpStatus.UNPROCESSABLE_CONTENT, "BOOK_NOT_AVAILABLE", "Cuốn sách này hiện đã ngừng kinh doanh"),
    OUT_OF_STOCK(HttpStatus.UNPROCESSABLE_CONTENT, "OUT_OF_STOCK", "Cuốn sách này hiện đã hết hàng"),
    INSUFFICIENT_STOCK(HttpStatus.UNPROCESSABLE_CONTENT, "INSUFFICIENT_STOCK", "Số lượng yêu cầu vượt quá tồn kho khả dụng"),
    CART_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_CONTENT, "CART_LIMIT_EXCEEDED", "Giỏ hàng đã đạt giới hạn tối đa 50 đầu sách khác nhau"),
    WISHLIST_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_CONTENT, "WISHLIST_LIMIT_EXCEEDED", "Danh sách yêu thích đã đạt giới hạn tối đa 200 cuốn sách");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}

