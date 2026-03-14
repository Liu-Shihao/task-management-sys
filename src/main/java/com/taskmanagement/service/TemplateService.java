package com.taskmanagement.service;

import com.taskmanagement.dto.request.CreateTemplateRequest;
import com.taskmanagement.dto.request.GenerateRundownRequest;
import com.taskmanagement.dto.response.RundownResponse;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.dto.response.TemplateResponse;
import com.taskmanagement.dto.response.TemplateTaskResponse;
import com.taskmanagement.entity.*;
import com.taskmanagement.exception.BusinessException;
import com.taskmanagement.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Template management service
 */
@Slf4j
@Service
public class TemplateService {

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
        final Template template = Template.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(userId)
                .build();

        // Add tasks
        if (request.getTasks() != null) {
            request.getTasks().forEach(taskRequest -> {
                TemplateTask task = TemplateTask.builder()
                        .name(taskRequest.getName())
                        .taskType(taskRequest.getTaskType())
                        .config(taskRequest.getConfig())
                        .orderIndex(taskRequest.getOrderIndex())
                        .build();
                template.addTask(task);
            });
        }

        Template savedTemplate = templateRepository.save(template);
        log.info("Template created with id: {}", savedTemplate.getId());

        return toResponse(savedTemplate);
    }

    /**
     * Clone template
     */
    @Transactional
    public TemplateResponse cloneTemplate(Long id, String newName, Long userId) {
        log.info("Cloning template: {}", id);

        Template source = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException.ResourceNotFoundException("Template", id));

        final Template clone = Template.builder()
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

        Template savedClone = templateRepository.save(clone);
        log.info("Template cloned with id: {}", savedClone.getId());

        return toResponse(savedClone);
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
                    .orderIndex(task.getOrderIndex())
                    .status(Task.STATUS_PENDING)
                    .build();
            rundown.addTask(newTask);
        });

        Rundown savedRundown = rundownRepository.save(rundown);
        log.info("Rundown generated with code: {}", rundownCode);

        return toRundownResponse(savedRundown);
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
        List<TemplateTaskResponse> tasks = template.getTasks().stream()
                .map(task -> TemplateTaskResponse.builder()
                        .id(task.getId())
                        .name(task.getName())
                        .taskType(task.getTaskType())
                        .config(task.getConfig())
                        .orderIndex(task.getOrderIndex())
                        .createdAt(task.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .taskCount(tasks.size())
                .tasks(tasks)
                .build();
    }

    private RundownResponse toRundownResponse(Rundown rundown) {
        List<TaskResponse> tasks = rundown.getTasks().stream()
                .map(task -> TaskResponse.builder()
                        .id(task.getId())
                        .name(task.getName())
                        .taskType(task.getTaskType())
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
                .taskCount(tasks.size())
                .tasks(tasks)
                .build();
    }
}
