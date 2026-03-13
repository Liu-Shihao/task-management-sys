package com.taskmanagement.exception;

import com.taskmanagement.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Business exception for application-level errors.
 * Can be thrown when business rules are violated.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.code = ErrorCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.code = ErrorCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(String message, String code) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.code = code;
    }

    public BusinessException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }
}