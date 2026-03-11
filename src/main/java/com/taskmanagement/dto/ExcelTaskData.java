package com.taskmanagement.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.taskmanagement.enums.TaskType;
import lombok.Data;

@Data
public class ExcelTaskData {

    @ExcelProperty("Task Name")
    private String taskName;

    @ExcelProperty("Task Type")
    private String taskType;

    @ExcelProperty("Sequence Order")
    private Integer sequenceOrder;

    @ExcelProperty("Config JSON")
    private String configJson;

    public TemplateTaskDTO toTemplateTaskDTO() {
        TemplateTaskDTO dto = new TemplateTaskDTO();
        dto.setTaskName(this.taskName);
        dto.setSequenceOrder(this.sequenceOrder != null ? this.sequenceOrder : 0);
        dto.setConfigJson(this.configJson);
        
        if (this.taskType != null) {
            try {
                dto.setTaskType(TaskType.valueOf(this.taskType.toUpperCase().trim()));
            } catch (IllegalArgumentException e) {
                dto.setTaskType(TaskType.MANUAL);
            }
        } else {
            dto.setTaskType(TaskType.MANUAL);
        }
        
        return dto;
    }
}
