package com.chungpr0.bookhub.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    @Builder.Default
    private boolean success = false;

    private int status;

    private String code;

    private String message;

    private List<FieldErrorItem> errors;

    private Object details;

    @Builder.Default
    private Object data = null;

    private String path;

    private String traceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
}

