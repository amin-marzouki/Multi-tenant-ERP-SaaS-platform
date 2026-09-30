package com.orbiterp.common;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
public class ApiError {
    String code;
    String message;
    Map<String, Object> details;
    String traceId;
    Instant timestamp;
}
