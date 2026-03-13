package com.taskmanagement.global;

import com.taskmanagement.dto.ErrorResponse;
import com.taskmanagement.enums.ErrorCode;
import com.taskmanagement.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.StringJoiner;

/**
 * Global exception handler for the application.
 * Catches and handles all exceptions thrown by controllers,
 * returning统一格式的错误响应.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String REQUEST_PATH_ATTR = "org.springframework.web.servlet.HandlerMapping.path";

    /**
     * Handle BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Business exception: code={}, message={}, path={}", ex.getCode(), ex.getMessage(), getPath(request));

        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .path(getPath(request))
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(error);
    }


    /**
     * Handle Exception (catch-all for any other exceptions)
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception: path={}", getPath(request), ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.SYSTEM_ERROR.getCode())
                .message(ErrorCode.SYSTEM_ERROR.getMessage())
                .path(getPath(request))
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.internalServerError().body(error);
    }


    /**
     * Get request path from HttpServletRequest
     */
    private String getPath(HttpServletRequest request) {
        String path = (String) request.getAttribute(REQUEST_PATH_ATTR);
        if (path == null) {
            path = request.getRequestURI();
        }
        return path;
    }
}