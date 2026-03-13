package com.taskmanagement.service;

import com.taskmanagement.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task management service
 */

@Service
@RequiredArgsConstructor
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    /**
     * Get tasks by rundown ID
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByRundown(Long rundownId) {
        return taskRepository.findByRundownIdOrderByOrderIndexAsc(rundownId);
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Task", id));
    }

    /**
     * Update task
     */
    @Transactional
    public Task updateTask(Long id, String name, String taskType, Map<String, Object> config) {
        log.info("Updating task: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Task", id));

        if (name != null) {
            task.setName(name);
        }
        if (taskType != null) {
            task.setTaskType(taskType);
        }
        if (config != null) {
            task.setConfig(config);
        }

        return taskRepository.save(task);
    }

    /**
     * Delete task
     */
    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new BusinessException.ResourceNotFoundException("Task", id);
        }

        taskRepository.deleteById(id);
        log.info("Task deleted: {}", id);
    }

    /**
     * Update task status
     */
    @Transactional
    public Task updateStatus(Long id, String status) {
        log.info("Updating task status: {} -> {}", id, status);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Task", id));

        task.setStatus(status);
        return taskRepository.save(task);
    }
}
