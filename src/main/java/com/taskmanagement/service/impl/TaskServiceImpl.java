package com.taskmanagement.service.impl;

import com.taskmanagement.dto.TaskRequest;
import com.taskmanagement.dto.TaskResponse;
import com.taskmanagement.entity.Task;
import com.taskmanagement.enums.TaskStatus;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.service.TaskService;
import com.taskmanagement.util.CronExpressionUtils;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        applyRequestToEntity(request, task);
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        
        // 计算下次执行时间（如果启用调度）
        calculateNextRunTime(task);
        
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findByIdOr404(id);
        applyRequestToEntity(request, task);
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        
        // 重新计算下次执行时间（如果调度配置发生变化）
        if (request.getSchedulingEnabled() != null || 
            request.getScheduledTime() != null || 
            request.getCronExpression() != null) {
            calculateNextRunTime(task);
        }
        
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = findByIdOr404(id);
        return toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(String rundownCode) {
        List<Task> tasks;
        if (rundownCode != null && !rundownCode.isBlank()) {
            tasks = taskRepository.findByRundownCodeOrderBySequenceNoAsc(rundownCode);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TaskResponse runTask(Long id) {
        Task task = findByIdOr404(id);

        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task is already in progress");
        }

        Instant now = Instant.now();
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(now);
        task.setLastMessage("Task execution started.");

        // 当前版本先做一个同步、快速的“模拟执行”
        task.setStatus(TaskStatus.COMPLETED);
        task.setFinishedAt(now);
        task.setLastMessage("Task executed successfully (mock).");

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    /**
     * 计算任务的下次执行时间
     * - Recurring (cronExpression): 根据 cron 表达式计算
     * - Onetime (scheduledTime): 使用指定的一次性时间
     * - 未启用调度: 清除下次执行时间
     */
    private void calculateNextRunTime(Task task) {
        if (!task.isSchedulingEnabled()) {
            task.setNextRunAt(null);
            return;
        }

        if (task.getCronExpression() != null && !task.getCronExpression().isBlank()) {
            // Recurring - 定时任务
            task.setNextRunAt(CronExpressionUtils.getNextExecution(task.getCronExpression()));
            log.info("创建定时任务，下次执行时间: taskId={}, nextRunAt={}", 
                    task.getId(), task.getNextRunAt());
        } else if (task.getScheduledTime() != null) {
            // Onetime - 单次任务
            task.setNextRunAt(task.getScheduledTime());
            log.info("创建单次任务，执行时间: taskId={}, scheduledTime={}", 
                    task.getId(), task.getScheduledTime());
        } else {
            // 启用了调度但没有配置时间
            task.setNextRunAt(null);
        }
    }

    private Task findByIdOr404(Long id) {
        return taskRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private void applyRequestToEntity(TaskRequest request, Task task) {
        task.setRundownCode(request.getRundownCode());
        task.setRundownName(request.getRundownName());
        task.setSequenceNo(request.getSequenceNo());
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setAutomationType(request.getAutomationType());
        task.setAutomationTarget(request.getAutomationTarget());
        task.setAutomationParamsJson(request.getAutomationParamsJson());

        if (request.getSchedulingEnabled() != null) {
            task.setSchedulingEnabled(request.getSchedulingEnabled());
        }
        task.setScheduledTime(request.getScheduledTime());
        task.setCronExpression(request.getCronExpression());
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse resp = new TaskResponse();
        resp.setId(task.getId());
        resp.setRundownCode(task.getRundownCode());
        resp.setRundownName(task.getRundownName());
        resp.setSequenceNo(task.getSequenceNo());
        resp.setName(task.getName());
        resp.setDescription(task.getDescription());
        resp.setStatus(task.getStatus());
        resp.setAutomationType(task.getAutomationType());
        resp.setAutomationTarget(task.getAutomationTarget());
        resp.setAutomationParamsJson(task.getAutomationParamsJson());
        resp.setSchedulingEnabled(task.isSchedulingEnabled());
        resp.setScheduledTime(task.getScheduledTime());
        resp.setCronExpression(task.getCronExpression());
        resp.setNextRunAt(task.getNextRunAt());
        resp.setLastScheduledRunAt(task.getLastScheduledRunAt());
        resp.setStartedAt(task.getStartedAt());
        resp.setFinishedAt(task.getFinishedAt());
        resp.setExternalExecutionId(task.getExternalExecutionId());
        resp.setExternalUrl(task.getExternalUrl());
        resp.setLastMessage(task.getLastMessage());
        resp.setCreatedAt(task.getCreatedAt());
        resp.setUpdatedAt(task.getUpdatedAt());
        return resp;
    }
}

