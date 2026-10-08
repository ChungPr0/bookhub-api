package com.chungpr0.bookhub.common.exception;

import com.chungpr0.bookhub.common.dto.FieldErrorItem;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends AppException {

    private static final long serialVersionUID = 1L;

    private final List<FieldErrorItem> errors;

    public ValidationException(List<FieldErrorItem> errors) {
        super(ErrorCode.VALIDATION_FAILED);
        this.errors = errors;
    }

    public ValidationException(String message, List<FieldErrorItem> errors) {
        super(ErrorCode.VALIDATION_FAILED, message);
        this.errors = errors;
    }
}

