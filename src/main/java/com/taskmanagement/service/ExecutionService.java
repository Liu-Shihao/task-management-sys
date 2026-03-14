package com.taskmanagement.service;

import com.taskmanagement.dto.response.RundownResponse;
import com.taskmanagement.entity.Rundown;
import com.taskmanagement.entity.Task;
import com.taskmanagement.executor.ExecutionResult;
import com.taskmanagement.executor.ExecutorFactory;
import com.taskmanagement.executor.TaskStatus;
import com.taskmanagement.repository.RundownRepository;
import com.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rundown execution service
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionService {

    private final RundownRepository rundownRepository;
    private final TaskRepository taskRepository;
    private final ExecutorFactory executorFactory;

    /**
     * Execute rundown sequentially
     */
    @Async("taskExecutor")
    @Transactional
    public void executeRundown(Long rundownId) {
        log.info("Starting rundown execution: {}", rundownId);

        Rundown rundown = rundownRepository.findById(rundownId)
                .orElseThrow(() -> new RuntimeException("Rundown not found: " + rundownId));

        // Update rundown status
        rundown.setStatus(Rundown.STATUS_RUNNING);
        rundown.setStartedAt(LocalDateTime.now());
        rundownRepository.save(rundown);

        // Get tasks in order
        List<Task> tasks = taskRepository.findByRundownIdOrderByOrderIndexAsc(rundownId);
        
        boolean allSuccess = true;
        boolean anySuccess = false;

        for (Task task : tasks) {
            log.info("Executing task: {} ({})", task.getName(), task.getId());

            // Update task status to running
            task.setStatus(Task.STATUS_RUNNING);
            task.setStartedAt(LocalDateTime.now());
            taskRepository.save(task);

            // Execute task
            ExecutionResult result = executorFactory.execute(task);

            // Update task with result
            task.setExternalId(result.getExecutionId() != null ? String.valueOf(result.getExecutionId()) : null);
            task.setExternalUrl(result.getExternalUrl());
            
            if (result.isSuccess()) {
                task.setStatus(Task.STATUS_SUCCESS);
                task.setFinishedAt(LocalDateTime.now());
                anySuccess = true;
                log.info("Task completed successfully: {}", task.getName());
            } else {
                task.setStatus(result.getStatus());
                task.setErrorMessage(result.getErrorMessage());
                task.setFinishedAt(LocalDateTime.now());
                allSuccess = false;
                log.error("Task failed: {}", task.getName());
                
                // Stop execution on failure
                break;
            }
            
            taskRepository.save(task);
        }

        // Update rundown final status
        rundown.setFinishedAt(LocalDateTime.now());
        
        if (allSuccess) {
            rundown.setStatus(Rundown.STATUS_SUCCESS);
        } else if (anySuccess) {
            rundown.setStatus(Rundown.STATUS_PARTIAL_SUCCESS);
        } else {
            rundown.setStatus(Task.STATUS_FAILED.equals(tasks.get(tasks.size() - 1).getStatus()) 
                    ? Rundown.STATUS_FAILED 
                    : Rundown.STATUS_ABORTED);
        }
        
        rundownRepository.save(rundown);
        log.info("Rundown execution completed: {} with status: {}", rundownId, rundown.getStatus());
    }

    /**
     * Execute single task
     */
    @Async("taskExecutor")
    @Transactional
    public void executeTask(Long taskId) {
        log.info("Starting task execution: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Update task status
        task.setStatus(Task.STATUS_RUNNING);
        task.setStartedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Execute task
        ExecutionResult result = executorFactory.execute(task);

        // Update task with result
        task.setExternalId(result.getExecutionId() != null ? String.valueOf(result.getExecutionId()) : null);
        task.setExternalUrl(result.getExternalUrl());
        
        if (result.isSuccess()) {
            task.setStatus(Task.STATUS_SUCCESS);
        } else {
            task.setStatus(result.getStatus());
            task.setErrorMessage(result.getErrorMessage());
        }
        
        task.setFinishedAt(LocalDateTime.now());
        taskRepository.save(task);

        log.info("Task execution completed: {} with status: {}", taskId, task.getStatus());
    }

    /**
     * Sync task status from external system
     */
    @Transactional
    public void syncTaskStatus(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getExternalId() == null) {
            return;
        }

        TaskStatus status = executorFactory.getStatus(task);
        if (status != null) {
            task.setStatus(status.getStatus());
            if (status.getExternalUrl() != null) {
                task.setExternalUrl(status.getExternalUrl());
            }
            taskRepository.save(task);
        }
    }
}
