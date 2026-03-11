package com.taskmanagement.dto;

import com.taskmanagement.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTaskDTO {

    private Long id;

    private Long templateId;

    @NotBlank(message = "Task name is required")
    private String taskName;

    @NotNull(message = "Task type is required")
    private TaskType taskType;

    @NotNull(message = "Sequence order is required")
    private Integer sequenceOrder;

    private String configJson;

    public TemplateTaskDTO(String taskName, TaskType taskType, Integer sequenceOrder) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.sequenceOrder = sequenceOrder;
    }
}
