package com.taskmanagement.service;

import com.taskmanagement.entity.Task;
import com.taskmanagement.exception.BusinessException;
import com.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Task management service
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

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
