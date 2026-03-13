package com.taskmanagement.executor;

import com.taskmanagement.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating task executors based on task type
 */
@Component
@RequiredArgsConstructor

public class ExecutorFactory {
    private static final Logger log = LoggerFactory.getLogger(ExecutorFactory.class);

    private final JenkinsExecutor jenkinsExecutor;
    private final AnsibleExecutor ansibleExecutor;

    /**
     * Get executor by task type
     */
    public TaskExecutor getExecutor(String taskType) {
        return switch (taskType.toLowerCase()) {
            case "jenkins" -> jenkinsExecutor;
            case "ansible" -> ansibleExecutor;
            default -> throw new IllegalArgumentException("Unsupported task type: " + taskType);
        };
    }

    /**
     * Execute task
     */
    public ExecutionResult execute(Task task) {
        TaskExecutor executor = getExecutor(task.getTaskType());
        return executor.execute(task);
    }

    /**
     * Get task status
     */
    public TaskStatus getStatus(Task task) {
        if (task.getExternalId() == null || task.getExternalId().isEmpty()) {
            return TaskStatus.builder().status(task.getStatus()).build();
        }
        
        TaskExecutor executor = getExecutor(task.getTaskType());
        return executor.getStatus(task.getExternalId());
    }

    /**
     * Cancel task
     */
    public boolean cancel(Task task) {
        if (task.getExternalId() == null || task.getExternalId().isEmpty()) {
            return false;
        }
        
        TaskExecutor executor = getExecutor(task.getTaskType());
        return executor.cancel(task.getExternalId());
    }

    /**
     * Get task logs
     */
    public String getLogs(Task task) {
        if (task.getExternalId() == null || task.getExternalId().isEmpty()) {
            return "";
        }
        
        TaskExecutor executor = getExecutor(task.getTaskType());
        return executor.getLogs(task.getExternalId());
    }
}
