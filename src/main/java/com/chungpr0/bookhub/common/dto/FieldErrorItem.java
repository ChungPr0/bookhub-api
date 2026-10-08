package com.chungpr0.bookhub.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorItem {
    private String field;
    private String code;
    private String message;
    private Object rejectedValue;
}

