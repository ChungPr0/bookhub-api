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
    WISHLIST_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_CONTENT, "WISHLIST_LIMIT_EXCEEDED", "Danh sách yêu thích đã đạt giới hạn tối đa 200 cuốn sách"),

    // MODULE 05 - ORDER, CHECKOUT, PAYMENT & SHIPPING
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng yêu cầu"),
    VOUCHER_NOT_FOUND(HttpStatus.NOT_FOUND, "VOUCHER_NOT_FOUND", "Không tìm thấy mã giảm giá yêu cầu"),
    VOUCHER_ALREADY_USED(HttpStatus.CONFLICT, "VOUCHER_ALREADY_USED", "Không thể sửa đổi hoặc xóa mã giảm giá đã phát sinh giao dịch"),
    PRICE_CHANGED(HttpStatus.CONFLICT, "PRICE_CHANGED", "Giá trị đơn hàng đã có sự thay đổi so với lúc xem trước. Vui lòng xác nhận lại"),
    ORDER_ALREADY_PAID(HttpStatus.CONFLICT, "ORDER_ALREADY_PAID", "Đơn hàng đã được thanh toán từ trước"),
    ORDER_CANNOT_BE_CANCELLED(HttpStatus.CONFLICT, "ORDER_CANNOT_BE_CANCELLED", "Đơn hàng không ở trạng thái cho phép hủy"),
    INVALID_ORDER_STATUS_TRANSITION(HttpStatus.CONFLICT, "INVALID_ORDER_STATUS_TRANSITION", "Chuyển trạng thái đơn hàng không hợp lệ"),
    CART_EMPTY(HttpStatus.UNPROCESSABLE_CONTENT, "CART_EMPTY", "Vui lòng chọn ít nhất một cuốn sách để thanh toán"),
    ITEMS_NOT_IN_CART(HttpStatus.UNPROCESSABLE_CONTENT, "ITEMS_NOT_IN_CART", "Một số sản phẩm không tồn tại trong giỏ hàng của bạn"),
    VOUCHER_USAGE_LIMIT_REACHED(HttpStatus.UNPROCESSABLE_CONTENT, "VOUCHER_USAGE_LIMIT_REACHED", "Mã giảm giá đã hết lượt sử dụng"),
    VOUCHER_MIN_ORDER_NOT_MET(HttpStatus.UNPROCESSABLE_CONTENT, "VOUCHER_MIN_ORDER_NOT_MET", "Đơn hàng chưa đạt giá trị tối thiểu của mã giảm giá"),
    VOUCHER_EXPIRED(HttpStatus.UNPROCESSABLE_CONTENT, "VOUCHER_EXPIRED", "Mã giảm giá đã hết hạn sử dụng"),
    VOUCHER_NOT_YET_VALID(HttpStatus.UNPROCESSABLE_CONTENT, "VOUCHER_NOT_YET_VALID", "Mã giảm giá chưa đến thời gian áp dụng"),
    INSUFFICIENT_POINTS(HttpStatus.UNPROCESSABLE_CONTENT, "INSUFFICIENT_POINTS", "Số dư điểm tích lũy không đủ để thực hiện giao dịch"),
    PAYMENT_EXPIRED(HttpStatus.UNPROCESSABLE_CONTENT, "PAYMENT_EXPIRED", "Đã quá thời hạn thanh toán đơn hàng online"),
    RETURN_WINDOW_EXPIRED(HttpStatus.UNPROCESSABLE_CONTENT, "RETURN_WINDOW_EXPIRED", "Đã quá thời hạn 7 ngày cho phép hoàn trả đơn hàng"),
    REFUND_NOT_APPLICABLE(HttpStatus.UNPROCESSABLE_CONTENT, "REFUND_NOT_APPLICABLE", "Đơn hàng không ở trạng thái chờ hoàn tiền"),
    LAST_PAYMENT_METHOD_PROTECTION(HttpStatus.UNPROCESSABLE_CONTENT, "LAST_PAYMENT_METHOD_PROTECTION", "Không thể vô hiệu hóa phương thức thanh toán đang hoạt động duy nhất"),
    SHIPPING_REGION_UNSUPPORTED(HttpStatus.UNPROCESSABLE_CONTENT, "SHIPPING_REGION_UNSUPPORTED", "Dịch vụ giao hàng không hỗ trợ địa bàn đã chọn"),
    PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY_ERROR", "Cổng thanh toán trực tuyến gặp sự cố"),

    // MODULE 06 - ADMIN CATALOG, REVIEWS & MEDIA
    CATEGORY_NAME_DUPLICATE(HttpStatus.CONFLICT, "CATEGORY_NAME_DUPLICATE", "Tên danh mục đã tồn tại trong nhóm này"),
    CATEGORY_HAS_CHILDREN(HttpStatus.CONFLICT, "CATEGORY_HAS_CHILDREN", "Không thể xóa: Vui lòng xóa hoặc di chuyển các danh mục con trước"),
    CATEGORY_HAS_BOOKS(HttpStatus.CONFLICT, "CATEGORY_HAS_BOOKS", "Không thể xóa: Danh mục này đang chứa sách"),
    AUTHOR_HAS_BOOKS(HttpStatus.CONFLICT, "AUTHOR_HAS_BOOKS", "Không thể xóa: Tác giả này đang được liên kết với sách trong hệ thống"),
    PUBLISHER_NAME_DUPLICATE(HttpStatus.CONFLICT, "PUBLISHER_NAME_DUPLICATE", "Tên nhà xuất bản đã tồn tại trong hệ thống"),
    PUBLISHER_HAS_BOOKS(HttpStatus.CONFLICT, "PUBLISHER_HAS_BOOKS", "Không thể xóa: Nhà xuất bản này đang được liên kết với sách trong hệ thống"),
    ISBN_ALREADY_EXISTS(HttpStatus.CONFLICT, "ISBN_ALREADY_EXISTS", "Mã ISBN đã thuộc về một cuốn sách khác"),
    BOOK_HAS_TRANSACTIONS(HttpStatus.CONFLICT, "BOOK_HAS_TRANSACTIONS", "Không thể xóa cuốn sách này vì đã phát sinh lịch sử giao dịch hoặc đơn hàng"),
    CATEGORY_MAX_DEPTH_EXCEEDED(HttpStatus.UNPROCESSABLE_CONTENT, "CATEGORY_MAX_DEPTH_EXCEEDED", "Hệ thống chỉ hỗ trợ tối đa 3 cấp danh mục"),
    CATEGORY_CIRCULAR_REFERENCE(HttpStatus.UNPROCESSABLE_CONTENT, "CATEGORY_CIRCULAR_REFERENCE", "Danh mục cha không thể là chính nó hoặc danh mục con cháu của nó"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "Không tìm thấy bài đánh giá yêu cầu"),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_ALREADY_EXISTS", "Bạn đã gửi đánh giá cho cuốn sách này trong đơn hàng trước đó"),
    REVIEW_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_CONTENT, "REVIEW_NOT_ALLOWED", "Chỉ được phép đánh giá hoặc chỉnh sửa sản phẩm từ đơn hàng đã hoàn tất"),
    REVIEW_PERIOD_EXPIRED(HttpStatus.UNPROCESSABLE_CONTENT, "REVIEW_PERIOD_EXPIRED", "Đã hết thời hạn 30 ngày cho phép thực hiện thao tác đánh giá"),
    FILE_REQUIRED(HttpStatus.BAD_REQUEST, "FILE_REQUIRED", "Vui lòng chọn tệp tin hình ảnh cần tải lên"),
    FILE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "FILE_TOO_LARGE", "Dung lượng ảnh vượt quá giới hạn tối đa 5MB"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_FILE_TYPE", "Hệ thống chỉ hỗ trợ định dạng JPG, PNG hoặc WEBP"),
    TOO_MANY_FILES(HttpStatus.UNPROCESSABLE_CONTENT, "TOO_MANY_FILES", "Số lượng ảnh tải lên cùng lúc vượt quá giới hạn tối đa 10 ảnh"),
    STORAGE_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "STORAGE_SERVICE_ERROR", "Lỗi kết nối dịch vụ lưu trữ media"),

    // MODULE 07 - INVENTORY & STOCK
    SUPPLIER_NOT_FOUND(HttpStatus.NOT_FOUND, "SUPPLIER_NOT_FOUND", "Không tìm thấy nhà cung cấp"),
    SUPPLIER_NAME_DUPLICATE(HttpStatus.CONFLICT, "SUPPLIER_NAME_DUPLICATE", "Tên nhà cung cấp đã tồn tại"),
    SUPPLIER_HAS_RECEIPTS(HttpStatus.CONFLICT, "SUPPLIER_HAS_RECEIPTS", "Không thể xóa nhà cung cấp đã có phiếu nhập kho"),
    STOCK_RECEIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_RECEIPT_NOT_FOUND", "Không tìm thấy phiếu nhập kho"),
    BATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "BATCH_NOT_FOUND", "Không tìm thấy lô hàng"),
    ADJUSTMENT_EXCEEDS_STOCK(HttpStatus.UNPROCESSABLE_CONTENT, "ADJUSTMENT_EXCEEDS_STOCK", "Số lượng điều chỉnh giảm vượt quá tồn kho hiện tại");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}

