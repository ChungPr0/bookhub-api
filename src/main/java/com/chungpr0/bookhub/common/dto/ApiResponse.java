package com.chungpr0.bookhub.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    @Builder.Default
    private int status = 200;

    @Builder.Default
    private String code = "SUCCESS";

    private String message;

    private T data;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Builder.Default
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

