package com.chungpr0.bookhub.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final Object details;
    private final Long retryAfterSeconds;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = null;
        this.retryAfterSeconds = null;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
        this.retryAfterSeconds = null;
    }

    public AppException(ErrorCode errorCode, Object details) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = details;
        this.retryAfterSeconds = null;
    }

    public AppException(ErrorCode errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
        this.retryAfterSeconds = null;
    }

    public AppException(ErrorCode errorCode, String message, Object details, Long retryAfterSeconds) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}

