package com.orbiterp.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

/** The one error body every endpoint returns: {@code { code, message, details, traceId }}. */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    String code;
    String message;
    Map<String, Object> details;
    String traceId;
    Instant timestamp;

    public static ApiError of(String code, String message, Map<String, Object> details) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .details(details)
                .traceId(MDC.get(TraceIdFilter.MDC_KEY))
                .timestamp(Instant.now())
                .build();
    }
}
