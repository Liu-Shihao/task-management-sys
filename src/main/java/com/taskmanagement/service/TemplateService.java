package com.taskmanagement.service;

import com.taskmanagement.dto.request.CreateTemplateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.request.GenerateRundownRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.response.RundownResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.response.TemplateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template management service
 */
@Service

public class TemplateService {
    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    private final TemplateRepository templateRepository;
    private final TemplateTaskRepository templateTaskRepository;
    private final RundownRepository rundownRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TemplateService(TemplateRepository templateRepository, 
                          TemplateTaskRepository templateTaskRepository,
                          RundownRepository rundownRepository,
                          TaskRepository taskRepository,
                          UserRepository userRepository) {
        this.templateRepository = templateRepository;
        this.templateTaskRepository = templateTaskRepository;
        this.rundownRepository = rundownRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all templates with pagination
     */
    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Get template by ID
     */
    @Transactional(readOnly = true)
    public TemplateResponse getTemplate(Long id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Template", id));
        return toResponse(template);
    }

    /**
     * Create new template
     */
    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request, Long userId) {
        log.info("Creating template: {}", request.getName());

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new BusinessException.ValidationException("User not found");
        }

        // Build template
        Template template = new Template();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCreatedBy(userId);

        // Add tasks
        request.getTasks().forEach(taskRequest -> {
            TemplateTask task = new TemplateTask();
            task.setName(taskRequest.getName());
            task.setTaskType(taskRequest.getTaskType());
            task.setConfig(taskRequest.getConfig());
            task.setOrderIndex(taskRequest.getOrderIndex());
            template.addTask(task);
        });

        template = templateRepository.save(template);
        log.info("Template created with id: {}", template.getId());

        return toResponse(template);
    }

    /**
     * Clone template
     */
    @Transactional
    public TemplateResponse cloneTemplate(Long id, String newName, Long userId) {
        log.info("Cloning template: {}", id);

        Template source = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Template", id));

        Template clone = Template.builder()
                .name(newName != null ? newName : source.getName() + " (Copy)")
                .description(source.getDescription())
                .createdBy(userId)
                .build();

        // Copy tasks
        source.getTasks().forEach(task -> {
            TemplateTask newTask = TemplateTask.builder()
                    .name(task.getName())
                    .taskType(task.getTaskType())
                    .config(task.getConfig())
                    .orderIndex(task.getOrderIndex())
                    .build();
            clone.addTask(newTask);
        });

        clone = templateRepository.save(clone);
        log.info("Template cloned with id: {}", clone.getId());

        return toResponse(clone);
    }

    /**
     * Generate rundown from template
     */
    @Transactional
    public RundownResponse generateRundown(Long templateId, GenerateRundownRequest request, Long userId) {
        log.info("Generating rundown from template: {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Template", templateId));

        // Generate rundown code
        String rundownCode = generateRundownCode();

        // Build rundown
        Rundown rundown = Rundown.builder()
                .rundownCode(rundownCode)
                .name(request.getName() != null ? request.getName() : template.getName())
                .description(request.getDescription() != null ? request.getDescription() : template.getDescription())
                .templateId(templateId)
                .status(Rundown.STATUS_PENDING)
                .createdBy(userId)
                .build();

        // Copy tasks from template
        template.getTasks().forEach(task -> {
            Task newTask = Task.builder()
                    .name(task.getName())
                    .taskType(task.getTaskType())
                    .config(task.getConfig())
                    .orderIndex(task.getOrderIndex())
                    .status(Task.STATUS_PENDING)
                    .build();
            rundown.addTask(newTask);
        });

        rundown = rundownRepository.save(rundown);
        log.info("Rundown generated with code: {}", rundownCode);

        return toRundownResponse(rundown);
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("Deleting template: {}", id);

        if (!templateRepository.existsById(id)) {
            throw new BusinessException.ResourceNotFoundException("Template", id);
        }

        // Check if any rundown references this template
        long rundownCount = rundownRepository.count();
        // Note: In production, check if any rundown has this template_id
        // For now, allow deletion

        templateRepository.deleteById(id);
        log.info("Template deleted: {}", id);
    }

    // Helper methods
    private String generateRundownCode() {
        String prefix = "RD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        
        String maxCode = rundownRepository.findMaxRundownCodeByPrefix(prefix).orElse(null);
        
        int nextNum = 1;
        if (maxCode != null) {
            String numStr = maxCode.substring(prefix.length());
            nextNum = Integer.parseInt(numStr) + 1;
        }
        
        return prefix + String.format("%03d", nextNum);
    }

    private TemplateResponse toResponse(Template template) {
        List<TemplateResponse.TemplateTaskResponse> tasks = template.getTasks().stream()
                .map(task -> TemplateResponse.TemplateTaskResponse.builder()
                        .id(task.getId())
                        .name(task.getName())
                        .taskType(task.getTaskType())
                        .config(task.getConfig())
                        .orderIndex(task.getOrderIndex())
                        .build())
                .collect(Collectors.toList());

        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .tasks(tasks)
                .build();
    }

    private RundownResponse toRundownResponse(Rundown rundown) {
        List<RundownResponse.TaskResponse> tasks = rundown.getTasks().stream()
                .map(task -> RundownResponse.TaskResponse.builder()
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
