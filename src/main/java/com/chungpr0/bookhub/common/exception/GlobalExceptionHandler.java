package com.chungpr0.bookhub.common.exception;

import com.chungpr0.bookhub.common.dto.ApiErrorResponse;
import com.chungpr0.bookhub.common.dto.FieldErrorItem;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.common.util.MaskingUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        String traceId = getTraceId(request);

        log.warn("AppException [{}]: {} (traceId: {})", errorCode.getCode(), ex.getMessage(), traceId);

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(ex.getMessage())
                .errors(null)
                .details(ex.getDetails())
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        HttpHeaders headers = new HttpHeaders();
        if (ex.getRetryAfterSeconds() != null) {
            headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()));
        }

        return new ResponseEntity<>(body, headers, errorCode.getHttpStatus());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        log.warn("ValidationException: {} (traceId: {})", ex.getMessage(), traceId);

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message(ex.getMessage())
                .errors(ex.getErrors())
                .details(null)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        List<FieldErrorItem> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String field = fieldError.getField();
            String code = mapAnnotationToFieldErrorCode(fieldError.getCode());
            String message = fieldError.getDefaultMessage();
            Object rejectedValue = MaskingUtils.maskIfSensitive(field, fieldError.getRejectedValue());

            errors.add(FieldErrorItem.builder()
                    .field(field)
                    .code(code)
                    .message(message)
                    .rejectedValue(rejectedValue)
                    .build());
        }

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message("Dữ liệu không hợp lệ, vui lòng kiểm tra lại")
                .errors(errors)
                .details(null)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        List<FieldErrorItem> errors = new ArrayList<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String field = violation.getPropertyPath().toString();
            String code = mapAnnotationToFieldErrorCode(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
            String message = violation.getMessage();
            Object rejectedValue = MaskingUtils.maskIfSensitive(field, violation.getInvalidValue());

            errors.add(FieldErrorItem.builder()
                    .field(field)
                    .code(code)
                    .message(message)
                    .rejectedValue(rejectedValue)
                    .build());
        }

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message("Dữ liệu không hợp lệ, vui lòng kiểm tra lại")
                .errors(errors)
                .details(null)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        log.warn("Malformed JSON request (traceId: {}): {}", traceId, ex.getMessage());

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.MALFORMED_REQUEST.getCode())
                .message(ErrorCode.MALFORMED_REQUEST.getDefaultMessage())
                .errors(null)
                .details(null)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", ex.getName());
        details.put("reason", "Sai kiểu dữ liệu, mong đợi kiểu " + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "hợp lệ"));

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_PARAMETER.getCode())
                .message("Tham số không hợp lệ: " + ex.getName())
                .errors(null)
                .details(details)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", ex.getParameterName());
        details.put("reason", "Tham số bắt buộc bị thiếu");

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_PARAMETER.getCode())
                .message("Thiếu tham số bắt buộc: " + ex.getParameterName())
                .errors(null)
                .details(details)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        Map<String, Object> details = new HashMap<>();
        details.put("allowed", ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods() : List.of());

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .code(ErrorCode.METHOD_NOT_ALLOWED.getCode())
                .message(ErrorCode.METHOD_NOT_ALLOWED.getDefaultMessage())
                .errors(null)
                .details(details)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.NOT_FOUND.value())
                .code(ErrorCode.ENDPOINT_NOT_FOUND.getCode())
                .message("Đường dẫn không tồn tại: " + request.getRequestURI())
                .errors(null)
                .details(null)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.FORBIDDEN.value())
                .code(ErrorCode.ACCESS_DENIED.getCode())
                .message(ErrorCode.ACCESS_DENIED.getDefaultMessage())
                .errors(null)
                .details(null)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.UNAUTHORIZED.value())
                .code(ErrorCode.UNAUTHENTICATED.getCode())
                .message(ErrorCode.UNAUTHENTICATED.getDefaultMessage())
                .errors(null)
                .details(null)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        log.error("Internal Server Error (traceId: {}): {}", traceId, ex.getMessage(), ex);

        ApiErrorResponse body = ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("Có lỗi xảy ra, vui lòng thử lại sau")
                .errors(null)
                .details(null)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String getTraceId(HttpServletRequest request) {
        String traceId = (String) request.getAttribute("traceId");
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        traceId = request.getHeader("X-Request-Id");
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        return UUID.randomUUID().toString();
    }

    private String mapAnnotationToFieldErrorCode(String constraintCode) {
        if (constraintCode == null) {
            return "INVALID_FORMAT";
        }
        return switch (constraintCode) {
            case "NotNull", "NotBlank", "NotEmpty" -> "REQUIRED";
            case "Email", "Pattern" -> "INVALID_FORMAT";
            case "Min", "Max", "Range" -> "OUT_OF_RANGE";
            case "Size", "Length" -> "TOO_SHORT";
            case "Past", "PastOrPresent" -> "MUST_BE_PAST";
            case "Future", "FutureOrPresent" -> "MUST_BE_FUTURE";
            default -> constraintCode.toUpperCase();
        };
    }
}

