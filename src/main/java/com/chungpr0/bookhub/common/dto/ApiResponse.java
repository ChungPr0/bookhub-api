package com.chungpr0.bookhub.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phong bì phản hồi API chuẩn hóa toàn hệ thống")
public class ApiResponse<T> {

    @Builder.Default
    @Schema(description = "Cờ trạng thái thành công của yêu cầu", example = "true")
    private boolean success = true;

    @Builder.Default
    @Schema(description = "Mã trạng thái HTTP", example = "200")
    private int status = 200;

    @Builder.Default
    @Schema(description = "Mã kết quả hoặc mã định danh nghiệp vụ", example = "SUCCESS")
    private String code = "SUCCESS";

    @Schema(description = "Thông điệp phản hồi thân thiện với người dùng", example = "Thao tác thành công")
    private String message;

    @Schema(description = "Dữ liệu phản hồi thực tế (payload)")
    private T data;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Builder.Default
    @Schema(description = "Thời điểm phản hồi theo chuẩn ISO-8601 (múi giờ +07:00)", example = "2026-10-09T17:00:00+07:00")
    private OffsetDateTime timestamp = OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .code("SUCCESS")
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
    }

    public static <T> ApiResponse<T> ok(String message) {
        return ok(message, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ok(message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return ok("Thao tác thành công", data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .code("CREATED")
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return created("Tạo mới thành công", data);
    }
}
