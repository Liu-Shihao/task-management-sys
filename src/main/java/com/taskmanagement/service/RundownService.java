package com.taskmanagement.service;

import com.taskmanagement.dto.response.RundownResponse;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.Rundown;
import com.taskmanagement.entity.Task;
import com.taskmanagement.exception.BusinessException;
import com.taskmanagement.repository.RundownRepository;
import com.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rundown management service
 */
@Service
@RequiredArgsConstructor
public class RundownService {
    
    private static final Logger log = LoggerFactory.getLogger(RundownService.class);

    private final RundownRepository rundownRepository;
    private final TaskRepository taskRepository;

    /**
     * Get all rundowns with pagination
     */
    @Transactional(readOnly = true)
    public Page<RundownResponse> getRundowns(Pageable pageable) {
        return rundownRepository.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Get rundown by ID
     */
    @Transactional(readOnly = true)
    public RundownResponse getRundown(Long id) {
        Rundown rundown = rundownRepository.findById(id)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Rundown", id));
        return toResponse(rundown);
    }

    /**
     * Get rundown by code
     */
    @Transactional(readOnly = true)
    public RundownResponse getRundownByCode(String code) {
        Rundown rundown = rundownRepository.findByRundownCode(code)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Rundown", code));
        return toResponse(rundown);
    }

    /**
     * Create blank rundown
     */
    @Transactional
    public RundownResponse createRundown(String name, String description, Long userId) {
        log.info("Creating rundown: {}", name);

        String rundownCode = generateRundownCode();

        Rundown rundown = Rundown.builder()
                .rundownCode(rundownCode)
                .name(name)
                .description(description)
                .status(Rundown.STATUS_PENDING)
                .createdBy(userId)
                .build();

        rundown = rundownRepository.save(rundown);
        log.info("Rundown created with code: {}", rundownCode);

        return toResponse(rundown);
    }

    /**
     * Update rundown
     */
    @Transactional
    public RundownResponse updateRundown(Long id, String name, String description) {
        log.info("Updating rundown: {}", id);

        Rundown rundown = rundownRepository.findById(id)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Rundown", id));

        if (name != null) {
            rundown.setName(name);
        }
        if (description != null) {
            rundown.setDescription(description);
        }

        rundown = rundownRepository.save(rundown);
        return toResponse(rundown);
    }

    /**
     * Delete rundown
     */
    @Transactional
    public void deleteRundown(Long id) {
        log.info("Deleting rundown: {}", id);

        if (!rundownRepository.existsById(id)) {
            throw new BusinessException.ResourceNotFoundException("Rundown", id);
        }

        rundownRepository.deleteById(id);
        log.info("Rundown deleted: {}", id);
    }

    /**
     * Add task to rundown
     */
    @Transactional
    public RundownResponse addTask(Long rundownId, Task task) {
        log.info("Adding task to rundown: {}", rundownId);

        Rundown rundown = rundownRepository.findById(rundownId)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Rundown", rundownId));

        // Set order index
        int maxOrder = rundown.getTasks().stream()
                .mapToInt(Task::getOrderIndex)
                .max()
                .orElse(0);
        task.setOrderIndex(maxOrder + 1);
        task.setStatus(Task.STATUS_PENDING);

        rundown.addTask(task);
        rundown = rundownRepository.save(rundown);

        return toResponse(rundown);
    }

    /**
     * Reorder tasks in rundown
     */
    @Transactional
    public RundownResponse reorderTasks(Long rundownId, List<Long> taskIds) {
        log.info("Reordering tasks for rundown: {}", rundownId);

        final Rundown rundown = rundownRepository.findById(rundownId)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Rundown", rundownId));

        // Update order index based on taskIds order
        for (int i = 0; i < taskIds.size(); i++) {
            final int order = i;
            final Long taskId = taskIds.get(i);
            rundown.getTasks().stream()
                    .filter(t -> t.getId().equals(taskId))
                    .findFirst()
                    .ifPresent(t -> t.setOrderIndex(order));
        }

        Rundown savedRundown = rundownRepository.save(rundown);
        return toResponse(savedRundown);
    }

    // Helper methods
    private String generateRundownCode() {
        String prefix = "RD-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        
        String maxCode = rundownRepository.findMaxRundownCodeByPrefix(prefix).orElse(null);
        
        int nextNum = 1;
        if (maxCode != null) {
            String numStr = maxCode.substring(prefix.length());
            nextNum = Integer.parseInt(numStr) + 1;
        }
        
        return prefix + String.format("%03d", nextNum);
    }

    private RundownResponse toResponse(Rundown rundown) {
        List<TaskResponse> tasks = rundown.getTasks().stream()
                .map(task -> TaskResponse.builder()
                        .id(task.getId())
                        .name(task.getName())
                        .taskType(task.getTaskType())
                        .config(task.getConfig())
                        .orderIndex(task.getOrderIndex())
                        .status(task.getStatus())
                        .externalId(task.getExternalId())
                        .externalUrl(task.getExternalUrl())
                        .errorMessage(task.getErrorMessage())
                        .createdAt(task.getCreatedAt())
                        .startedAt(task.getStartedAt())
                        .finishedAt(task.getFinishedAt())
                        .build())
                .collect(Collectors.toList());

        return RundownResponse.builder()
                .id(rundown.getId())
                .rundownCode(rundown.getRundownCode())
                .name(rundown.getName())
                .description(rundown.getDescription())
                .templateId(rundown.getTemplateId())
                .status(rundown.getStatus())
                .createdBy(rundown.getCreatedBy())
                .createdAt(rundown.getCreatedAt())
                .updatedAt(rundown.getUpdatedAt())
                .startedAt(rundown.getStartedAt())
                .finishedAt(rundown.getFinishedAt())
                .scheduleType(rundown.getScheduleType())
                .cronExpression(rundown.getCronExpression())
                .runTime(rundown.getRunTime())
                .scheduleStatus(rundown.getScheduleStatus())
                .nextRunTime(rundown.getNextRunTime())
                .lastRunAt(rundown.getLastRunAt())
                .tasks(tasks)
                .build();
    }
}
