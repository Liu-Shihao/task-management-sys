package com.taskmanagement.exception;

/**
 * Base exception for business errors
 */
public class BusinessException extends RuntimeException {

    private final String code;
    private final String message;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    // Common exceptions
    public static class ResourceNotFoundException extends BusinessException {
        public ResourceNotFoundException(String resource, Long id) {
            super("RESOURCE_NOT_FOUND", resource + " not found with id: " + id);
        }

        public ResourceNotFoundException(String resource, String code) {
            super("RESOURCE_NOT_FOUND", resource + " not found with code: " + code);
        }
    }

    public static class ValidationException extends BusinessException {
        public ValidationException(String message) {
            super("VALIDATION_ERROR", message);
        }
    }

    public static class UnauthorizedException extends BusinessException {
        public UnauthorizedException(String message) {
            super("UNAUTHORIZED", message);
        }
    }

    public static class ExternalSystemException extends BusinessException {
        public ExternalSystemException(String system, String message) {
            super("EXTERNAL_ERROR", system + " error: " + message);
        }
    }
}
