package com.taskmanagement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Global error code enum.
 *
 * Format: ERROR_CATEGORY_XXX
 * - SYSTEM: System errors
 * - VALIDATION: Validation errors
 * - NOT_FOUND: Resource not found
 * - CONFLICT: Conflict errors
 * - BUSINESS: Business errors
 * - AUTH: Authentication/Authorization errors
 * - EXCEL: Excel processing errors
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // System errors
    SYSTEM_ERROR("SYSTEM_ERROR", "System error, please try again later"),
    SYSTEM_TIMEOUT("SYSTEM_TIMEOUT", "System timeout, please try again later"),
    SYSTEM_UNAVAILABLE("SYSTEM_UNAVAILABLE", "Service unavailable, please try again later"),

    // Validation errors
    VALIDATION_ERROR("VALIDATION_ERROR", "Parameter validation failed"),
    INVALID_PARAMETER("INVALID_PARAMETER", "Invalid parameter"),
    MISSING_PARAMETER("MISSING_PARAMETER", "Missing required parameter"),

    // Resource not found
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource not found"),
    TASK_NOT_FOUND("TASK_NOT_FOUND", "Task not found"),
    UPLOAD_LOG_NOT_FOUND("UPLOAD_LOG_NOT_FOUND", "Upload log not found"),

    // Conflict errors
    RESOURCE_CONFLICT("RESOURCE_CONFLICT", "Resource state conflict"),
    TASK_ALREADY_COMPLETED("TASK_ALREADY_COMPLETED", "Task already completed"),
    TASK_ALREADY_IN_PROGRESS("TASK_ALREADY_IN_PROGRESS", "Task already in progress"),

    // Business errors
    BUSINESS_ERROR("BUSINESS_ERROR", "Business processing failed"),
    INVALID_TASK_STATUS("INVALID_TASK_STATUS", "Invalid task status"),
    INVALID_SCHEDULE_MODE("INVALID_SCHEDULE_MODE", "Invalid schedule mode"),
    INVALID_AUTOMATION_TYPE("INVALID_AUTOMATION_TYPE", "Invalid automation type"),
    INVALID_CRON_EXPRESSION("INVALID_CRON_EXPRESSION", "Invalid cron expression"),
    SCHEDULED_TIME_IN_PAST("SCHEDULED_TIME_IN_PAST", "Scheduled time cannot be in the past"),

    // Authentication/Authorization errors
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized access"),
    FORBIDDEN("FORBIDDEN", "No access permission"),

    // Excel processing errors
    EXCEL_PARSE_ERROR("EXCEL_PARSE_ERROR", "Failed to parse Excel file"),
    EXCEL_FORMAT_ERROR("EXCEL_FORMAT_ERROR", "Excel file format error"),
    EXCEL_EMPTY_FILE("EXCEL_EMPTY_FILE", "Excel file is empty"),
    EXCEL_EMPTY_SHEET("EXCEL_EMPTY_SHEET", "Excel worksheet is empty"),
    EXCEL_ROW_ERROR("EXCEL_ROW_ERROR", "Excel row data format error"),

    // External service errors
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "External service call failed"),
    ;

    private final String code;

    private final String message;
}