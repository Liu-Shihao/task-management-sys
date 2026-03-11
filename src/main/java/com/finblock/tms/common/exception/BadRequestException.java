package com.finblock.tms.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public BadRequestException(String message, Map<String, Object> details) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST", details);
    }
}

