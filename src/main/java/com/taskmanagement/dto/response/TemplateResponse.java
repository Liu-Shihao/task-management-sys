package com.taskmanagement.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Template response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {

    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int taskCount;
    private List<TemplateTaskResponse> tasks;

    public static TemplateResponse fromEntity(com.taskmanagement.entity.Template template) {
        return TemplateResponse.builder()
            .id(template.getId())
            .name(template.getName())
            .description(template.getDescription())
            .createdBy(template.getCreatedBy())
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .taskCount(template.getTasks() != null ? template.getTasks().size() : 0)
            .tasks(template.getTasks() != null ? template.getTasks().stream()
                .map(t -> TemplateTaskResponse.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .taskType(t.getTaskType())
                    .orderIndex(t.getOrderIndex())
                    .config(t.getConfig())
                    .createdAt(t.getCreatedAt())
                    .build())
                .collect(Collectors.toList()) : null)
            .build();
    }
}
