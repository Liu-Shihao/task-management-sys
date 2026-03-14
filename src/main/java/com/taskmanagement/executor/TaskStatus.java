package com.taskmanagement.executor;

import lombok.*;

/**
 * Task status DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatus {

    private Long taskId;
    private String status;
    private String message;
    private String externalUrl;

    public static TaskStatus pending(Long taskId) {
        return TaskStatus.builder().taskId(taskId).status("pending").build();
    }

    public static TaskStatus running(Long taskId) {
        return TaskStatus.builder().taskId(taskId).status("running").build();
    }

    public static TaskStatus success(Long taskId) {
        return TaskStatus.builder().taskId(taskId).status("success").message("Task completed successfully").build();
    }

    public static TaskStatus failed(Long taskId, String message) {
        return TaskStatus.builder().taskId(taskId).status("failed").message(message).build();
    }
}
