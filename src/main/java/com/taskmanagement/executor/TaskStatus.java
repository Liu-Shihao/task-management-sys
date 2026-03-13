package com.taskmanagement.executor;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task status DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatus {
    private static final Logger log = LoggerFactory.getLogger(TaskStatus.class);

    private Long taskId;
    private String status;
    private String message;

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
