package com.taskmanagement.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Template response DTO
 */
public class TemplateResponse {

    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int taskCount;
    private List<TaskInfo> tasks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getTaskCount() { return taskCount; }
    public void setTaskCount(int taskCount) { this.taskCount = taskCount; }

    public List<TaskInfo> getTasks() { return tasks; }
    public void setTasks(List<TaskInfo> tasks) { this.tasks = tasks; }

    public static TemplateResponse fromEntity(com.taskmanagement.entity.Template template) {
        TemplateResponse response = new TemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setCreatedBy(template.getCreatedBy());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        response.setTaskCount(template.getTasks() != null ? template.getTasks().size() : 0);
        
        if (template.getTasks() != null) {
            response.setTasks(template.getTasks().stream()
                .map(t -> {
                    TaskInfo info = new TaskInfo();
                    info.setId(t.getId());
                    info.setName(t.getName());
                    info.setTaskType(t.getTaskType());
                    info.setOrderIndex(t.getOrderIndex());
                    return info;
                })
                .collect(Collectors.toList()));
        }
        
        return response;
    }

    public static class TaskInfo {
        private Long id;
        private String name;
        private String taskType;
        private Integer orderIndex;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }

        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    }
}
