package com.taskmanagement.global;

import com.taskmanagement.dto.ErrorResponse;
import com.taskmanagement.enums.ErrorCode;
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
        log.warn("Business exception: code={}, message={}, path={}", ex.getCode(), ex.getMessage(), getPath(request), ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .path(getPath(request))
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    /**
     * Handle ResponseStatusException (including @ResponseStatus annotated exceptions)
     */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            org.springframework.web.server.ResponseStatusException ex, HttpServletRequest request) {
        log.warn("Response status exception: status={}, message={}, path={}", ex.getStatus(), ex.getMessage(), getPath(request), ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(getErrorCodeFromStatus(ex.getStatus()))
                .message(ex.getMessage())
                .path(getPath(request))
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    /**
     * Handle MethodArgumentNotValidException (validation errors for @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = extractFieldErrors(ex);
        log.warn("Validation error: details={}, path={}", details, getPath(request), ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .details(details)
                .path(getPath(request))
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle BindException (validation errors for @Validated)
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        String details = extractFieldErrors(ex);
        log.warn("Bind exception: details={}, path={}", details, getPath(request), ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .details(details)
                .path(getPath(request))
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle MissingServletRequestParameterException
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing parameter exception: name={}, type={}, path={}", ex.getParameterName(), ex.getParameterType(), getPath(request), ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.MISSING_PARAMETER.getCode())
                .message(ErrorCode.MISSING_PARAMETER.getMessage() + ": " + ex.getParameterName())
                .path(getPath(request))
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(error);
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
     * Extract field errors from validation exception
     */
    private String extractFieldErrors(Exception ex) {
        StringJoiner joiner = new StringJoiner("; ");

        if (ex instanceof MethodArgumentNotValidException methodEx) {
            methodEx.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                String errorMsg = error.getDefaultMessage();
                if (errorMsg != null) {
                    joiner.add(fieldName + ": " + errorMsg);
                }
            });
        } else if (ex instanceof BindException bindEx) {
            bindEx.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                String errorMsg = error.getDefaultMessage();
                if (errorMsg != null) {
                    joiner.add(fieldName + ": " + errorMsg);
                }
            });
        }

        return joiner.toString();
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

    /**
     * Get error code from HTTP status
     */
    private String getErrorCodeFromStatus(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> ErrorCode.RESOURCE_NOT_FOUND.getCode();
            case CONFLICT -> ErrorCode.RESOURCE_CONFLICT.getCode();
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED.getCode();
            case FORBIDDEN -> ErrorCode.FORBIDDEN.getCode();
            case BAD_REQUEST -> ErrorCode.INVALID_PARAMETER.getCode();
            default -> ErrorCode.SYSTEM_ERROR.getCode();
        };
    }
}