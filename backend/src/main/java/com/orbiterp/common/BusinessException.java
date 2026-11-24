package com.orbiterp.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** A business rule was broken; the code and message go back to the client as they are. */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /** The request conflicts with the current state: wrong status, not enough stock, a duplicate. */
    public static BusinessException conflict(String code, String message) {
        return new BusinessException(HttpStatus.CONFLICT, code, message);
    }

    /** The request is well formed but its values are not acceptable. */
    public static BusinessException invalid(String code, String message) {
        return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}
