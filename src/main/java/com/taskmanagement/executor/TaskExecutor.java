package com.taskmanagement.executor;

import com.taskmanagement.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task executor interface
 */
public interface TaskExecutor {

    /**
     * Execute a task
     */
    ExecutionResult execute(Task task);

    /**
     * Get task status from external system
     */
    TaskStatus getStatus(String executionId);

    /**
     * Cancel a running task
     */
    boolean cancel(String executionId);

    /**
     * Get execution logs
     */
    String getLogs(String executionId);
}
