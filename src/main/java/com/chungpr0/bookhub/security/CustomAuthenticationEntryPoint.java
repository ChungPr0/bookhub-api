package com.chungpr0.bookhub.security;

import com.chungpr0.bookhub.common.dto.ApiErrorResponse;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.config.TraceIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ErrorCode errorCode = (ErrorCode) request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTR);
        Object details = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_DETAILS_ATTR);

        if (errorCode == null) {
            errorCode = ErrorCode.UNAUTHENTICATED;
        }

        int httpStatus = errorCode.getHttpStatus().value();
        String traceId = (String) request.getAttribute(TraceIdFilter.MDC_TRACE_ID_KEY);
        if (traceId == null) {
            traceId = response.getHeader(TraceIdFilter.REQUEST_ID_HEADER);
        }

        log.warn("Unauthorized access to {} [{}]: {}", request.getRequestURI(), errorCode.getCode(), authException != null ? authException.getMessage() : "No auth");

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .success(false)
                .status(httpStatus)
                .code(errorCode.getCode())
                .message(errorCode.getDefaultMessage())
                .errors(null)
                .details(details)
                .data(null)
                .path(request.getRequestURI())
                .traceId(traceId)
                .timestamp(DateTimeUtils.nowVietnam())
                .build();

        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

