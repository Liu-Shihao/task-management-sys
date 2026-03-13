package com.taskmanagement.executor;

import com.taskmanagement.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execution result DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {
    private static final Logger log = LoggerFactory.getLogger(ExecutionResult.class);

    private boolean success;
    private String message;
    private Long taskId;
    private String externalId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static ExecutionResult success() {
        return ExecutionResult.builder().success(true).message("Execution completed successfully").build();
    }

    public static ExecutionResult success(Task task) {
        return ExecutionResult.builder()
                .success(true)
                .taskId(task.getId())
                .message("Task completed successfully")
                .endTime(LocalDateTime.now())
                .build();
    }

    public static ExecutionResult failed(String message) {
        return ExecutionResult.builder().success(false).message(message).build();
    }

    public static ExecutionResult failed(Task task, String message) {
        return ExecutionResult.builder()
                .success(false)
                .taskId(task.getId())
                .message(message)
                .endTime(LocalDateTime.now())
                .build();
    }

    public static ExecutionResult aborted(Task task) {
        return ExecutionResult.builder()
                .success(false)
                .taskId(task.getId())
                .message("Task aborted due to previous failure")
                .endTime(LocalDateTime.now())
                .build();
    }

    // Aliases for service layer compatibility
    public Long getExecutionId() { return externalId != null ? (long) externalId.hashCode() : null; }
    public String getExternalUrl() { return externalId; }
    public String getErrorMessage() { return success ? null : message; }
    public String getStatus() { return success ? "success" : "failed"; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
