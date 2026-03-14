package com.taskmanagement.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * Create template request DTO
 */
public class CreateTemplateRequest {

    @Size(max = 100, message = "Template name must be less than 100 characters")
    private String name;

    private String description;

    @NotEmpty(message = "Task list cannot be empty")
    private List<TaskConfig> tasks;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<TaskConfig> getTasks() { return tasks; }
    public void setTasks(List<TaskConfig> tasks) { this.tasks = tasks; }

    public static class TaskConfig {
        private String name;
        private String taskType;
        private Integer orderIndex;
        private Map<String, Object> config;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }

        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }
}
