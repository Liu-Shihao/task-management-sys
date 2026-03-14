package com.taskmanagement.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rundown response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RundownResponse {

    private Long id;
    private String rundownCode;
    private String name;
    private String description;
    private Long templateId;
    private String status;
    private String scheduleType;
    private String cronExpression;
    private LocalDateTime runTime;
    private String scheduleStatus;
    private LocalDateTime nextRunTime;
    private LocalDateTime lastRunAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int taskCount;
    private List<TaskResponse> tasks;

    public static RundownResponse fromEntity(com.taskmanagement.entity.Rundown rundown) {
        return RundownResponse.builder()
            .id(rundown.getId())
            .rundownCode(rundown.getRundownCode())
            .name(rundown.getName())
            .description(rundown.getDescription())
            .templateId(rundown.getTemplateId())
            .status(rundown.getStatus())
            .scheduleType(rundown.getScheduleType())
            .cronExpression(rundown.getCronExpression())
            .runTime(rundown.getRunTime())
            .scheduleStatus(rundown.getScheduleStatus())
            .nextRunTime(rundown.getNextRunTime())
            .lastRunAt(rundown.getLastRunAt())
            .startedAt(rundown.getStartedAt())
            .finishedAt(rundown.getFinishedAt())
            .createdBy(rundown.getCreatedBy())
            .createdAt(rundown.getCreatedAt())
            .updatedAt(rundown.getUpdatedAt())
            .taskCount(rundown.getTasks() != null ? rundown.getTasks().size() : 0)
            .tasks(rundown.getTasks() != null ? rundown.getTasks().stream()
                .map(t -> TaskResponse.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .taskType(t.getTaskType())
                    .orderIndex(t.getOrderIndex())
                    .config(t.getConfig())
                    .status(t.getStatus())
                    .externalId(t.getExternalId())
                    .externalUrl(t.getExternalUrl())
                    .errorMessage(t.getErrorMessage())
                    .startedAt(t.getStartedAt())
                    .finishedAt(t.getFinishedAt())
                    .createdAt(t.getCreatedAt())
                    .updatedAt(t.getUpdatedAt())
                    .build())
                .collect(Collectors.toList()) : null)
            .build();
    }
}
